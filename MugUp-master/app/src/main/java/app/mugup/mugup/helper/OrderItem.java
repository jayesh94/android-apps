package app.mugup.mugup.helper;

public class OrderItem {

    String purchase_timestamp, amount, status, subjects, invoiceId;

    public OrderItem(String purchase_timestamp, String amount, String status, String subjects, String invoiceId) {
        this.purchase_timestamp = purchase_timestamp;
        this.amount = amount;
        this.status = status;
        this.subjects = subjects;
        this.invoiceId = invoiceId;
    }

    public String getPurchase_timestamp() {
        return purchase_timestamp;
    }

    public void setPurchase_timestamp(String purchase_timestamp) {
        this.purchase_timestamp = purchase_timestamp;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSubjects() {
        return subjects;
    }

    public void setSubjects(String subjects) {
        this.subjects = subjects;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }
}
