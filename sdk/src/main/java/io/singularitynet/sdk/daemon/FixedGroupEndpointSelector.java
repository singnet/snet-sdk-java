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

    private final String endpointGroupName;

    /**
     * @param endpointGroupName name of the endpoint group to connect.
     */
    public FixedGroupEndpointSelector(String endpointGroupName) {
        this.endpointGroupName = endpointGroupName;
    }

    @Override
    public Endpoint nextEndpoint(MetadataProvider metadataProvider) {
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
