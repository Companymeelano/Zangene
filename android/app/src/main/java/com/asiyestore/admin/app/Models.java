package com.asiyestore.admin.app;

import java.util.ArrayList;
import java.util.List;

public final class Models {

    private Models() {}

    public static final class Product {
        public long id;
        public String sku;
        public String ttacSerial;
        public String batchCode;
        public String expiryLabel;
        public boolean coldChain;
        public int replenishDays;
        public String nameFa;
        public String nameEn;
        public String category;
        public String description;
        public String ingredients;
        public String usageMethod;
        public String country;
        public String volume;
        public long originalPrice;
        public long salePrice;
        public int discountPercent;
        public int stock;
        public int lowStockThreshold;
        public boolean published;
        public String imageKey;
        public boolean wishlisted;
        // ── v6.0 AI Studio enrichment ──
        public String benefits;          // bullet lines separated by \n
        public String longDescription;   // full persuasive story
        public String seoTitle;
        public String seoDescription;
        public String keywords;
        public String instagramCaption;
        public String imagePath;         // absolute path of AI studio render
        public boolean aiGenerated;
    }


    /** A verified customer review (moderated by the boutique owner). */
    public static final class ReviewRecord {
        public long id;
        public long productId;
        public String author;
        public int rating;
        public String body;
        public boolean approved;
        public String createdLabel;
    }

    /** Aggregated star rating for a product or the whole boutique. */
    public static final class RatingSummary {
        public double average;
        public int count;
    }

    /** One selectable BNPL / installment plan computed by InstallmentEngine. */
    public static final class InstallmentPlan {
        public String id;
        public String titleFa;
        public String providerFa;
        public int months;
        public double interestPercent;
        public long downPayment;
        public long financedAmount;
        public long monthlyPayment;
        public long payableTotal;
        public long totalFee;
        public boolean zeroInterest;
        public String noteFa;
    }

    /** Payment gateway row shown in the customer checkout + admin payment settings. */
    public static final class PaymentGatewayRecord {
        public String code;
        public String titleFa;
        public String subtitleFa;
        public boolean enabled;
        public int feePercent;
    }

    public static final class CartItem {
        public Product product;
        public String selectedVariant;
        public int quantity;

        public long lineTotal() {
            return product.salePrice * quantity;
        }
    }

    public static final class OrderItemRecord {
        public long productId;
        public String productName;
        public String variantLabel;
        public int quantity;
        public long unitPrice;
    }

    public static final class OrderRecord {
        public long id;
        public String orderNumber;
        public String customerName;
        public String customerMobile;
        public String addressSummary;
        public String shippingMethod;
        public String paymentMethod;
        public int installmentsCount;
        public long subtotal;
        public long discount;
        public long walletUsed;
        public long shippingCost;
        public long totalAmount;
        public long cashbackEarned;
        public boolean vipGiftBox;
        public boolean coldChain;
        public String scentedTissue;
        public String giftMessage;
        public String certificateCode;
        public String status;
        public String trackingCode;
        public String createdAtLabel;
        public List<OrderItemRecord> items = new ArrayList<>();
    }

    public static final class WalletTransactionRecord {
        public long id;
        public long amount; // + credit, - debit
        public String type;
        public String description;
        public String createdAtLabel;
    }

    public static final class CertificateRecord {
        public long id;
        public String certCode;
        public String productName;
        public String sku;
        public String batchCode;
        public String ownerName;
        public String issuedAtLabel;
    }

    public static final class StockMovementRecord {
        public long id;
        public long productId;
        public String productName;
        public int deltaQuantity;
        public String type;
        public String reason;
        public String actor;
        public String createdAtLabel;
    }

    public static final class CouponRecord {
        public long id;
        public String code;
        public int discountPercent;
        public long discountAmount;
        public long minPurchase;
        public long maxDiscount;
        public boolean active;
    }

    /** v6.5 · a saved delivery address from the customer's address book. */
    public static final class AddressRecord {
        public long id;
        public String label;
        public String name;
        public String mobile;
        public String address;
        public boolean isDefault;
    }

    /** v6.5 · one row of the merchant CRM, aggregated from real orders. */
    public static final class CustomerRecord {
        public String name;
        public String mobile;
        public int ordersCount;
        public long totalSpend;
        public String lastOrderLabel;
        public String lastStatus;
    }

    /** v6.5 · one line of the customer's notification centre. */
    public static final class NotificationRecord {
        public int icon;
        public String title;
        public String body;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  v6.8 · ACCOUNTS · OTP · SMS OUTBOX
    // ══════════════════════════════════════════════════════════════════════
    public static final class AccountRecord {
        public long id;
        public String mobile = "";
        public String name = "";
        public String email = "";
        public String skinType = "نامشخص";
        public String birthday = "";
        public boolean smsOptin = true;
        public long createdAtMs;
        public long lastLoginMs;
        public int ordersCount;
        public long totalSpent;
    }

    public static final class SmsMessage {
        public long id;
        public String mobile = "";
        public String templateKey = "";
        public String body = "";
        public String status = "pending";
        public int attempts;
        public long createdAtMs;
        public long sentAtMs;
        public String error = "";
    }

    public static final class SmsTemplateRecord {
        public String key = "";
        public String title = "";
        public String body = "";
        public boolean enabled = true;
    }

    public static final class ReorderDue {
        public String mobile = "";
        public String name = "";
        public String productName = "";
        public String lastLabel = "";
        public int days;
    }

    public static final class TopProductRecord {
        public String productName;
        public int unitsSold;
        public long revenue;
    }

    public static final class DashboardSnapshot {
        public long totalRevenue;
        public long avgOrderValue;
        public long salesToday;
        public long salesWeek;
        public long salesYesterday;
        public int activeOrdersCount;
        public int totalOrdersCount;
        public int todayOrdersCount;
        public int lowStockCount;
        public int installmentOrdersCount;
        public int vipGiftOrdersCount;
        public int totalProductsCount;
        public long[] sevenDayRevenue = new long[7];
        public String[] sevenDayLabels = new String[7];
    }
}
