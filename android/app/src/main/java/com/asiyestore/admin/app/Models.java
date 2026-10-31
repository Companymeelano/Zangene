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
        public String imageKey; // "serum", "caviar", "lipstick", "perfume", "elixir", "giftbox"
        public boolean wishlisted;
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

    public static final class DashboardSnapshot {
        public long salesToday;
        public long salesWeek;
        public int activeOrdersCount;
        public int totalOrdersCount;
        public int lowStockCount;
        public int installmentOrdersCount;
        public int vipGiftOrdersCount;
        public int totalProductsCount;
        public long[] sevenDayRevenue = new long[7];
        public String[] sevenDayLabels = new String[7];
    }
}
