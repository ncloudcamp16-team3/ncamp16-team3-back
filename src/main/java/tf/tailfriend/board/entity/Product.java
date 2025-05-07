package tf.tailfriend.board.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "products")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Product {

    @Id
    @Column(name = "board_id")
    private Integer id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "board_id")
    private Board board;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private Boolean sell = true;

    private String address;

    public void updateProduct(Integer price, Boolean sell, String addres) {
        this.price = price;
        this.sell = sell;
        this.address = addres;
    }
}
