package io.singularitynet.sdk.daemon;

import com.google.protobuf.ByteString;
import java.math.BigInteger;

import io.singularitynet.sdk.common.Utils;

public class GrpcUtils {

    private GrpcUtils() {
    }

    public static ByteString toBytesString(BigInteger value) {
        return ByteString.copyFrom(Utils.bigIntToBytes32(value));
    }

    public static BigInteger toBigInt(ByteString value) {
        return Utils.bytes32ToBigInt(value.toByteArray());
    }

}
