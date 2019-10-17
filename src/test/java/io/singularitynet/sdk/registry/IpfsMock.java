package io.singularitynet.sdk.registry;

import io.ipfs.api.IPFS;
import io.ipfs.multihash.Multihash;
import javax.json.*;
import static org.mockito.Mockito.*;

import static io.singularitynet.sdk.registry.Utils.*;

public class IpfsMock {

    private IPFS ipfs = mock(IPFS.class);

    public IPFS getIpfs() {
        return ipfs;
    }

    public ReturnMock<JsonObjectBuilder> cat(String hash) {
        return new ReturnMock<JsonObjectBuilder>() {
            public IpfsMock returns(JsonObjectBuilder value) {
                return rethrowChecked(() -> {
                    when(ipfs.cat(eq(Multihash.fromBase58(hash))))
                        .thenReturn(strToBytes(value.build().toString()));
                    return IpfsMock.this;
                });
            }
        };
    }

    public static interface ReturnMock<T> {
        IpfsMock returns(T value);
    }

    public static JsonObjectBuilder serviceMetadataJson(int port) {
        return Json.createObjectBuilder()
            .add("version", "1")
            .add("display_name", "Test Service Name")
            .add("encoding", "proto")
            .add("service_type", "grpc")
            .add("model_ipfs_hash", "QmR3anSdm4s13iLt3zzyrSbtvCDJNwhkrYG6yFGFHXBznb")
            .add("mpe_address", "0x8FB1dC8df86b388C7e00689d1eCb533A160B4D0C")
            .add("groups", Json.createArrayBuilder()
                    .add(Json.createObjectBuilder()
                        .add("group_name", "default_group")
                        .add("pricing", Json.createArrayBuilder()
                            .add(Json.createObjectBuilder()
                                .add("price_model", "fixed_price")
                                .add("price_in_cogs", 1)
                                .add("default", true)
                                .build())
                            .build())
                        .add("endpoints", Json.createArrayBuilder()
                            .add("http://localhost:" + port)
                            .build())
                        .add("group_id", "m5FKWq4hW0foGW5qSbzGSjgZRuKs7A1ZwbIrJ9e96rc=")
                        .build())
                    .build())
            .add("assets", Json.createObjectBuilder().build())
            .add("service_description", Json.createObjectBuilder()
                    .add("url", "https://singnet.github.io/dnn-model-services/users_guide/i3d-video-action-recognition.html")
                    .add("description", "This service uses I3D to perform action recognition on videos.")
                    .build());
    }
}

