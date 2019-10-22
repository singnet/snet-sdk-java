package io.singularitynet.sdk.mpe;

import io.grpc.Metadata;

public interface Payment {

    void toMetadata(Metadata headers);

}
