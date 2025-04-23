package tf.tailfriend.global.service;

import tf.tailfriend.facility.dto.FacilityCard;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SortAndPage {
    public List<FacilityCard> sortAndPage(List<FacilityCard> data, String sortBy, int page, int size) {
        Comparator<FacilityCard> comparator;

        switch (sortBy) {
            case "rating":
                comparator = Comparator.comparing(FacilityCard::getRating).reversed();
                break;
            case "distance":
                comparator = Comparator.comparing(FacilityCard::getDistance);
                break;
            default:
                comparator = Comparator.comparing(FacilityCard::getId); // default sort
        }

        List<FacilityCard> sorted = data.stream()
                .sorted(comparator)
                .toList();

        int start = page * size;
        int end = Math.min(start + size, sorted.size());

        return (start >= sorted.size()) ? Collections.emptyList() : sorted.subList(start, end);
    }
}
