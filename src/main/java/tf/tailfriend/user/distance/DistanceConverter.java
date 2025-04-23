package tf.tailfriend.user.distance;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DistanceConverter implements AttributeConverter<Distance, String> {

    @Override
    public String convertToDatabaseColumn(Distance attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public Distance convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return Distance.fromCode(dbData);
    }
}
