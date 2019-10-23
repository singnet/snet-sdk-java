package io.singularitynet.sdk.registry;

import java.util.List;
import java.util.Collections;
import org.web3j.protocol.core.*;
import org.web3j.tuples.generated.*;
import io.singularitynet.sdk.contracts.Registry;
import static org.mockito.Mockito.*;

import static io.singularitynet.sdk.registry.Utils.*;

public class RegistryMock {

    private final Registry registry = mock(Registry.class);

    public Registry get() {
        return registry;
    }

    public ReturnMock<GetServiceRegistrationByIdResultBuider> getServiceRegistrationById(String orgId, String serviceId) {
        return new ReturnMock<GetServiceRegistrationByIdResultBuider>() {
            public RegistryMock returns(GetServiceRegistrationByIdResultBuider value) {
                when(registry.getServiceRegistrationById(eq(strToBytes32(orgId)), eq(strToBytes32(serviceId))))
                    .thenReturn(value.build());
                return RegistryMock.this;
            }
        };
    }

    public static interface ReturnMock<T> {
        RegistryMock returns(T value);
    }

    public static class GetServiceRegistrationByIdResultBuider {

        private Boolean found;
        private byte[] id;
        private byte[] metadataUri;

        public GetServiceRegistrationByIdResultBuider() {
        }

        public GetServiceRegistrationByIdResultBuider setFound(Boolean found) {
            this.found = found;
            return this;
        }

        public GetServiceRegistrationByIdResultBuider setId(String id) {
            this.id = strToBytes32(id);
            return this;
        }

        public GetServiceRegistrationByIdResultBuider setMetadataUri(String metadataUri) {
            this.metadataUri = strToBytes(metadataUri);
            return this;
        }

        public RemoteFunctionCall<Tuple4<Boolean, byte[], byte[], List<byte[]>>> build() {
            return new RemoteFunctionCall<>(null,
                    () -> {
                        return new Tuple4<>(found, id, metadataUri, Collections.EMPTY_LIST);
            });
        }
    }

    public static GetServiceRegistrationByIdResultBuider serviceRegistration() {
        return new GetServiceRegistrationByIdResultBuider()
            .setFound(true)
            .setId("test-service-id")
            .setMetadataUri("ipfs://QmR3anSdm4s13iLt3zzyrSbtvCDJNwhkrYG6yFGFHXBznb");
    }
}


