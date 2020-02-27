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

// FIXME: add javadoc
public class FreeCallPayment implements Payment {

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

    private final String userId;
    private final BigInteger tokenExpirationBlock;
    private final byte[] token;
    private final BigInteger currentBlockNumber;
    private final Signature signature;

    @Override
    public void toMetadata(Metadata headers) {
        headers.put(PaymentSerializer.SNET_PAYMENT_TYPE, PAYMENT_TYPE_FREE_CALL);
        headers.put(SNET_FREE_CALL_USER_ID, userId);
        headers.put(SNET_FREE_CALL_TOKEN_EXPIRY_BLOCK, tokenExpirationBlock);
        headers.put(SNET_FREE_CALL_AUTH_TOKEN, token);
        headers.put(SNET_CURRENT_BLOCK_NUMBER, currentBlockNumber);
        headers.put(PaymentSerializer.SNET_PAYMENT_SIGNATURE, signature.getBytes());
    }

    public static FreeCallPayment fromMetadata(Metadata headers) {
        String userId = headers.get(SNET_FREE_CALL_USER_ID);
        BigInteger tokenExpirationBlock = headers.get(SNET_FREE_CALL_TOKEN_EXPIRY_BLOCK);
        byte[] token = headers.get(SNET_FREE_CALL_AUTH_TOKEN);
        BigInteger currentBlockNumber = headers.get(SNET_CURRENT_BLOCK_NUMBER);
        byte[] signature = headers.get(PaymentSerializer.SNET_PAYMENT_SIGNATURE);
        return new FreeCallPayment(userId, tokenExpirationBlock, token,
                currentBlockNumber, new Signature(signature));
    }

    public FreeCallPayment(String userId, BigInteger tokenExpirationBlock,
            byte[] token, BigInteger currentBlockNumber, Signature signature) {
        this.userId = userId;
        this.tokenExpirationBlock = tokenExpirationBlock;
        this.token = token;
        this.currentBlockNumber = currentBlockNumber;
        this.signature = signature;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private Identity signer;
        private String dappUserId;
        private BigInteger tokenExpirationBlock;
        private byte[] token;
        private BigInteger currentBlockNumber;
        private String orgId;
        private String serviceId;
        private PaymentGroupId paymentGroupId;

        public Builder setSigner(Identity signer) {
            this.signer = signer;
            return this;
        }

        public Builder setDappUserId(String dappUserId) {
            this.dappUserId = dappUserId;
            return this;
        }

        public Builder setTokenExpirationBlock(BigInteger tokenExpirationBlock) {
            this.tokenExpirationBlock = tokenExpirationBlock;
            return this;
        }

        // FIXME: use String here to allow user writing own strategy
        public Builder setToken(byte[] token) {
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
            return new FreeCallPayment(dappUserId, tokenExpirationBlock,
                    token, currentBlockNumber, signature);
        }

        private static final byte[] FREE_CALL_MESSAGE_PREFIX = Utils.strToBytes("__prefix_free_trial");

        private byte[] getMessage() {
            return Utils.wrapExceptions(() -> {
                ByteArrayOutputStream message = new ByteArrayOutputStream();
                message.write(FREE_CALL_MESSAGE_PREFIX);
                message.write(Utils.strToBytes(dappUserId));
                message.write(Utils.strToBytes(orgId));
                message.write(Utils.strToBytes(serviceId));
                message.write(Utils.strToBytes(paymentGroupId.toString()));
                message.write(Utils.bigIntToBytes32(currentBlockNumber));
                message.write(token);
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
