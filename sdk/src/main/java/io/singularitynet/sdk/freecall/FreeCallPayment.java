package io.singularitynet.sdk.freecall;

import io.grpc.Metadata;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;

import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.payment.Payment;
import io.singularitynet.sdk.payment.PaymentSerializer;
import io.singularitynet.sdk.ethereum.Address;
import io.singularitynet.sdk.ethereum.Identity;
import io.singularitynet.sdk.ethereum.Signature;
import io.singularitynet.sdk.registry.PaymentGroupId;

/**
 * Free call payment implementation. Payment contains DApp user id, free call
 * token and signature of the Ethereum identity which is owned by DApp user id.
 */
public class FreeCallPayment implements Payment {

    /**
     * Free call payment type.
     */
    public static final String PAYMENT_TYPE_FREE_CALL = "free-call";

    static {
        PaymentSerializer.register(PAYMENT_TYPE_FREE_CALL, FreeCallPayment::fromMetadata);
    }

    private static final Metadata.Key<String> SNET_FREE_CALL_USER_ID =
        Metadata.Key.of("snet-free-call-user-id", Metadata.ASCII_STRING_MARSHALLER);
    private static final Metadata.Key<BigInteger> SNET_FREE_CALL_TOKEN_EXPIRY_BLOCK =
        Metadata.Key.of("snet-free-call-token-expiry-block", PaymentSerializer.ASCII_BIGINTEGER_MARSHALLER);
    private static final Metadata.Key<byte[]> SNET_FREE_CALL_AUTH_TOKEN =
        Metadata.Key.of("snet-free-call-auth-token" + Metadata.BINARY_HEADER_SUFFIX, Metadata.BINARY_BYTE_MARSHALLER);
    private static final Metadata.Key<BigInteger> SNET_CURRENT_BLOCK_NUMBER =
        Metadata.Key.of("snet-current-block-number", PaymentSerializer.ASCII_BIGINTEGER_MARSHALLER);

    private final String dappUserId;
    private final BigInteger tokenExpirationBlock;
    private final byte[] token;
    private final BigInteger currentBlockNumber;
    private final Signature signature;

    @Override
    public void toMetadata(Metadata headers) {
        headers.put(PaymentSerializer.SNET_PAYMENT_TYPE, PAYMENT_TYPE_FREE_CALL);
        headers.put(SNET_FREE_CALL_USER_ID, dappUserId);
        headers.put(SNET_FREE_CALL_TOKEN_EXPIRY_BLOCK, tokenExpirationBlock);
        headers.put(SNET_FREE_CALL_AUTH_TOKEN, token);
        headers.put(SNET_CURRENT_BLOCK_NUMBER, currentBlockNumber);
        headers.put(PaymentSerializer.SNET_PAYMENT_SIGNATURE, signature.getBytes());
    }

    /**
     * Load free call payment from gRPC metadata
     * @param headers gRPC metadata
     * @return free call payment instance
     */
    public static FreeCallPayment fromMetadata(Metadata headers) {
        String dappUserId = headers.get(SNET_FREE_CALL_USER_ID);
        BigInteger tokenExpirationBlock = headers.get(SNET_FREE_CALL_TOKEN_EXPIRY_BLOCK);
        byte[] token = headers.get(SNET_FREE_CALL_AUTH_TOKEN);
        BigInteger currentBlockNumber = headers.get(SNET_CURRENT_BLOCK_NUMBER);
        byte[] signature = headers.get(PaymentSerializer.SNET_PAYMENT_SIGNATURE);
        return new FreeCallPayment(dappUserId, tokenExpirationBlock, token,
                currentBlockNumber, new Signature(signature));
    }

    public FreeCallPayment(String dappUserId, BigInteger tokenExpirationBlock,
            byte[] token, BigInteger currentBlockNumber, Signature signature) {
        this.dappUserId = dappUserId;
        this.tokenExpirationBlock = tokenExpirationBlock;
        this.token = token;
        this.currentBlockNumber = currentBlockNumber;
        this.signature = signature;
    }

    public String getDappUserId() {
        return dappUserId;
    }

    public BigInteger getTokenExpirationBlock() {
        return tokenExpirationBlock;
    }

    public byte[] getToken() {
        return token;
    }

    public BigInteger getCurrentBlockNumber() {
        return currentBlockNumber;
    }

    public Signature getSignature() {
        return signature;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private Identity signer;
        private FreeCallAuthToken token;
        private BigInteger currentBlockNumber;
        private String orgId;
        private String serviceId;
        private PaymentGroupId paymentGroupId;

        public Builder setSigner(Identity signer) {
            this.signer = signer;
            return this;
        }

        public Builder setToken(FreeCallAuthToken token) {
            this.token = token;
            return this;
        }

        public Builder setCurrentBlockNumber(BigInteger currentBlockNumber) {
            this.currentBlockNumber = currentBlockNumber;
            return this;
        }

        public Builder setOrgId(String orgId) {
            this.orgId = orgId;
            return this;
        }

        public Builder setServiceId(String serviceId) {
            this.serviceId = serviceId;
            return this;
        }

        public Builder setPaymentGroupId(PaymentGroupId paymentGroupId) {
            this.paymentGroupId = paymentGroupId;
            return this;
        }

        public FreeCallPayment build() {
            byte[] message = getMessage();
            Signature signature = signer.sign(message);
            return new FreeCallPayment(token.getDappUserId(),
                    token.getExpirationBlock(),
                    Utils.hexToBytes(token.getToken()),
                    currentBlockNumber, signature);
        }

        private static final byte[] FREE_CALL_MESSAGE_PREFIX = Utils.strToBytes("__prefix_free_trial");

        private byte[] getMessage() {
            return Utils.wrapExceptions(() -> {
                ByteArrayOutputStream message = new ByteArrayOutputStream();
                message.write(FREE_CALL_MESSAGE_PREFIX);
                message.write(Utils.strToBytes(token.getDappUserId()));
                message.write(Utils.strToBytes(orgId));
                message.write(Utils.strToBytes(serviceId));
                message.write(Utils.strToBytes(paymentGroupId.toString()));
                message.write(Utils.bigIntToBytes32(currentBlockNumber));
                message.write(Utils.hexToBytes(token.getToken()));
                return message.toByteArray();
            });
        }
    }

    public static String generateFreeCallPaymentToken(String dappUserId,
            Address userEthereumAddress, BigInteger expirationBlockNumber,
            Identity signer) {
        return Utils.wrapExceptions(() -> {
            ByteArrayOutputStream message = new ByteArrayOutputStream();
            message.write(Utils.strToBytes(dappUserId));
            message.write(userEthereumAddress.toByteArray());
            message.write(Utils.bigIntToBytes32(expirationBlockNumber));
            Signature signature = signer.sign(message.toByteArray());
            return Utils.bytesToHex(signature.getBytes());
        });
    }
}
