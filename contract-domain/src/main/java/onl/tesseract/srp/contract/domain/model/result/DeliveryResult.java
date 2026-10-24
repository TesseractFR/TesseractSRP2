package onl.tesseract.srp.contract.domain.model.result;

public record DeliveryResult(DeliveryStatus status, int reward, int missing) {

    public static DeliveryResult delivered(int reward) {
        return new DeliveryResult(DeliveryStatus.DELIVERED, reward, 0);
    }

    public static DeliveryResult failed(DeliveryStatus status) {
        return new DeliveryResult(status, 0, 0);
    }

    public static DeliveryResult missing(DeliveryStatus status, int missing) {
        return new DeliveryResult(status, 0, missing);
    }

    public boolean isDelivered() {
        return status == DeliveryStatus.DELIVERED;
    }
}