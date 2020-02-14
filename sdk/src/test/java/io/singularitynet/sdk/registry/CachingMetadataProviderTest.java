package io.singularitynet.sdk.registry;

import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CachingMetadataProviderTest {

    @Test
    public void getOrganizationMetadataOnce() {
        OrganizationMetadata orgMetadata = mock(OrganizationMetadata.class);
        MetadataProvider original = mock(MetadataProvider.class);
        when(original.getOrganizationMetadata()).thenReturn(orgMetadata);

        CachingMetadataProvider provider = new CachingMetadataProvider(original);
        OrganizationMetadata result;

        result = provider.getOrganizationMetadata();
        assertEquals("Organization metadata sample", orgMetadata, result);
        verify(original, times(1)).getOrganizationMetadata();

        result = provider.getOrganizationMetadata();
        assertEquals("Organization metadata sample", orgMetadata, result);
        verify(original, times(1)).getOrganizationMetadata();
    }

}
