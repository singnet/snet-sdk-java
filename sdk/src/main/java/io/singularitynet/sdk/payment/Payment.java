package io.singularitynet.sdk.payment;

import io.grpc.Metadata;

/**
 * This interface is a payment abstraction. Payment is a piece of information
 * which required by daemon to accept a client call. It is not necessary
 * related to paying something. For example subscription payment contains
 * information which can be used to prove subscription status. Each kind of
 * payment contains specific data fields and uses specific serialization
 * format.
 */
public interface Payment {

    /**
     * Serialize payment information to the gRPC metadata in order to pass it
     * to the daemon.
     * @param headers gRPC call metadata to put payment information.
     */
    void toMetadata(Metadata headers);

}
