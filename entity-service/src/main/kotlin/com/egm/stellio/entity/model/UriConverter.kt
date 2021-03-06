package com.egm.stellio.entity.model

import com.egm.stellio.shared.util.toUri
import org.neo4j.ogm.typeconversion.AttributeConverter
import java.net.URI

class UriConverter : AttributeConverter<URI, String> {

    override fun toGraphProperty(value: URI?): String? {
        return value?.toString()
    }

    override fun toEntityAttribute(value: String?): URI? {
        return value?.let {
            value.toUri()
        }
    }
}
