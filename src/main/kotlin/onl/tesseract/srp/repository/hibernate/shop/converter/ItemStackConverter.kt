package onl.tesseract.srp.repository.hibernate.shop.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.bukkit.inventory.ItemStack
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.type.TypeReference

@Converter
class ItemStackConverter : AttributeConverter<ItemStack, String> {

    private val objectMapper = ObjectMapper()

    override fun convertToDatabaseColumn(attribute: ItemStack?): String? {
        if (attribute == null) return null
        // Serialize ItemStack to Map and then to JSON string
        val map = attribute.serialize()
        return objectMapper.writeValueAsString(map)
    }

    override fun convertToEntityAttribute(dbData: String?): ItemStack? {
        if (dbData == null || dbData.isEmpty()) return null
        val map: Map<String, Any> = objectMapper.readValue(dbData, object : TypeReference<Map<String, Any>>() {})
        return ItemStack.deserialize(map)
    }
}
