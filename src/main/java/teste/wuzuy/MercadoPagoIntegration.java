package teste.wuzuy;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.common.IdentificationRequest;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.resources.payment.Payment;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

public class MercadoPagoIntegration {

    final static String accessToken = System.getenv("MP_TOKEN");

    static {
        MercadoPagoConfig.setAccessToken(accessToken);
    }

    public static String createPixPayment() {
        Map<String, String> customHeaders = new HashMap<>();
        customHeaders.put("x-idempotency-key", "<SOME_UNIQUE_VALUE>");

        MPRequestOptions requestOptions = MPRequestOptions.builder()
                .customHeaders(customHeaders)
                .build();

        PaymentClient client = new PaymentClient();

        PaymentCreateRequest paymentCreateRequest = PaymentCreateRequest.builder()
                .transactionAmount(new BigDecimal("1"))
                .description("i o pix")
                .paymentMethodId("pix") // Especifica o método de pagamento como Pix
                .dateOfExpiration(OffsetDateTime.now().plusDays(1)) // Expiração do Pix
                .payer(
                        PaymentPayerRequest.builder()
                                .email("ffxtreme7@gmail.com")
                                .firstName("Lucas")
                                .identification(
                                        IdentificationRequest.builder().type("CPF").number("19119119100").build())
                                .build())
                .build();

        try {
            Payment payment = client.create(paymentCreateRequest, requestOptions);
            return payment.getPointOfInteraction().getTransactionData().getQrCode();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}