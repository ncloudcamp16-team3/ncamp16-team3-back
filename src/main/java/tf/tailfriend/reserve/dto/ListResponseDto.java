package tf.tailfriend.reserve.dto;

import lombok.*;
import org.springframework.data.domain.Slice;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListResponseDto<T> {

    private List<T> data;
    private Integer currentPage;
    private Integer size;
    private boolean last;
    private long totalElements;

}