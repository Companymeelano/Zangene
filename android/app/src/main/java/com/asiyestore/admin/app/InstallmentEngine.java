package com.asiyestore.admin.app;

import java.util.ArrayList;
import java.util.List;

/**
 * ASIYE STORE — Professional Buy-Now-Pay-Later / Installment Engine (v6.0).
 *
 * Reads merchant configuration from the settings table so the boutique owner
 * can tune every financial parameter from the admin panel:
 *   inst_enabled · inst_zero_months · inst_extended_months · inst_interest_percent
 *   inst_min_amount · inst_max_amount · inst_down_percent · inst_provider · inst_fee_payer
 */
public final class InstallmentEngine {

    public static final class Config {
        public boolean enabled;
        public int zeroMonths;
        public int[] extendedMonths;
        public double annualInterestPercent;
        public long minAmount;
        public long maxAmount;
        public int downPaymentPercent;
        public String providerCode;
        public String providerLabel;
        public String feePayer;
    }

    private InstallmentEngine() { }

    public static Config load(AsiyeDatabaseHelper db) {
        Config c = new Config();
        c.enabled = db.getSettingBool("inst_enabled", true);
        c.zeroMonths = Math.max(2, db.getSettingInt("inst_zero_months", 4));
        c.annualInterestPercent = parseDouble(db.getSetting("inst_interest_percent", "18"), 18);
        c.minAmount = db.getSettingLong("inst_min_amount", 1_000_000L);
        c.maxAmount = db.getSettingLong("inst_max_amount", 20_000_000L);
        c.downPaymentPercent = Math.max(0, Math.min(70, db.getSettingInt("inst_down_percent", 0)));
        c.providerCode = db.getSetting("inst_provider", "snapp_pay");
        c.providerLabel = providerLabel(c.providerCode);
        c.feePayer = db.getSetting("inst_fee_payer", "merchant");

        String raw = db.getSetting("inst_extended_months", "6,12");
        List<Integer> months = new ArrayList<>();
        for (String part : PersianFormatter.toAsciiDigits(raw).split("[,\\u060C\\s]+")) {
            try {
                int m = Integer.parseInt(part.trim());
                if (m >= 2 && m <= 24) months.add(m);
            } catch (Exception ignored) { }
        }
        if (months.isEmpty()) months.add(6);
        c.extendedMonths = new int[months.size()];
        for (int i = 0; i < months.size(); i++) c.extendedMonths[i] = months.get(i);
        return c;
    }

    public static String providerLabel(String code) {
        if ("snapp_pay".equals(code)) return "اسنپ‌پی";
        if ("tara".equals(code)) return "اعتبار خرید تارا";
        if ("digipay".equals(code)) return "دیجی‌پی";
        if ("azkivam".equals(code)) return "ازکی‌وام";
        if ("bank".equals(code)) return "کارت اعتباری بانکی";
        return "اعتبار خرید مجموعه";
    }

    /** Returns an empty list when the basket amount is outside the merchant's BNPL window. */
    public static List<Models.InstallmentPlan> build(Config cfg, long amount) {
        List<Models.InstallmentPlan> plans = new ArrayList<>();
        if (!cfg.enabled || amount <= 0 || amount < cfg.minAmount || amount > cfg.maxAmount) return plans;

        long down = Math.round(amount * (cfg.downPaymentPercent / 100.0));

        Models.InstallmentPlan zero = new Models.InstallmentPlan();
        zero.id = "inst_zero";
        zero.titleFa = PersianFormatter.toPersianDigits(String.valueOf(cfg.zeroMonths)) + " قسط بدون کارمزد";
        zero.providerFa = cfg.providerLabel;
        zero.months = cfg.zeroMonths;
        zero.interestPercent = 0;
        zero.downPayment = down;
        zero.financedAmount = amount - down;
        zero.monthlyPayment = (long) Math.ceil(zero.financedAmount / (double) zero.months);
        zero.payableTotal = amount;
        zero.totalFee = 0L;
        zero.zeroInterest = true;
        boolean zeroSplit = zero.financedAmount % zero.months == 0L;
        zero.noteFa = zeroSplit
                ? "تقسیم مساوی مبلغ • بدون کارمزد و بدون بهره"
                : "بدون کارمزد و بهره • قسط آخر اندکی کمتر";
        plans.add(zero);

        for (int months : cfg.extendedMonths) {
            Models.InstallmentPlan p = new Models.InstallmentPlan();
            p.id = "inst_" + months;
            p.titleFa = PersianFormatter.toPersianDigits(String.valueOf(months)) + " قسط ماهانه";
            p.providerFa = cfg.providerLabel;
            p.months = months;
            p.interestPercent = cfg.annualInterestPercent;
            p.downPayment = down;
            p.financedAmount = amount - down;
            double feeRate = cfg.annualInterestPercent / 100.0 * (months / 12.0);
            long fee = Math.round(p.financedAmount * feeRate);
            // monthly and total are derived from the same number so the receipt,
            // the plan card and the repayment table can never disagree.
            long monthly = (long) Math.ceil((p.financedAmount + fee) / (double) months);
            p.monthlyPayment = monthly;
            p.payableTotal = monthly * months;
            p.totalFee = p.payableTotal - p.financedAmount;
            if (p.totalFee < 0) p.totalFee = 0;
            p.zeroInterest = false;
            p.noteFa = "کارمزد سالانه " + PersianFormatter.toPersianDigits(String.valueOf((int) cfg.annualInterestPercent)) + "٪";
            plans.add(p);
        }
        return plans;
    }

    public static Models.InstallmentPlan find(List<Models.InstallmentPlan> plans, String id) {
        if (plans == null) return null;
        for (Models.InstallmentPlan p : plans) {
            if (p.id.equals(id)) return p;
        }
        return null;
    }

    private static double parseDouble(String raw, double fallback) {
        try {
            return Double.parseDouble(PersianFormatter.toAsciiDigits(raw).replace("٪", "").trim());
        } catch (Exception e) {
            return fallback;
        }
    }
}
