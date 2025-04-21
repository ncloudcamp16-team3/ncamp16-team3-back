package tf.tailfriend.reserve.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Slice;

import java.util.List;

@Getter
@Setter
@Builder
public class ListResponseDto<T> {

    private List<T> data;
    private Integer currentPage;
    private Integer size;
    private Integer totalPages;
    private long totalElements;

}