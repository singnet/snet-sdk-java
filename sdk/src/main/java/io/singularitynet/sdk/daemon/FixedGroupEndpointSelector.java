package io.singularitynet.sdk.daemon;

import java.net.URL;
import java.util.List;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.singularitynet.sdk.common.Utils;
import io.singularitynet.sdk.registry.MetadataProvider;
import io.singularitynet.sdk.registry.ServiceMetadata;
import io.singularitynet.sdk.registry.EndpointGroup;

/**
 * Endpoint selection strategy which returns random endpoint within specified
 * endpoint group.
 */
@ToString
public class FixedGroupEndpointSelector implements EndpointSelector {

    private final static Logger log = LoggerFactory.getLogger(FixedGroupEndpointSelector.class);

    @ToString.Exclude
    private final MetadataProvider metadataProvider;
    private final String endpointGroupName;

    /**
     * @param metadataProvider service metadata provider.
     * @param endpointGroupName name of the endpoint group to connect.
     */
    public FixedGroupEndpointSelector(MetadataProvider metadataProvider,
            String endpointGroupName) {
        this.metadataProvider = metadataProvider;
        this.endpointGroupName = endpointGroupName;
    }

    @Override
    public Endpoint nextEndpoint() {
        ServiceMetadata serviceMetadata = metadataProvider.getServiceMetadata();
        EndpointGroup group = serviceMetadata
            .getEndpointGroupByName(endpointGroupName).get();
        URL url =  Utils.getRandomItem(group.getEndpoints());
        log.info("Next endpoint selected: {}", url);
        return new Endpoint() {

            public EndpointGroup getGroup() {
                return group;
            }

            public URL getUrl() {
                return url;
            }

        };
    }

}
