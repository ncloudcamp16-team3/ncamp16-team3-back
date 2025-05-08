package tf.tailfriend.board.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import tf.tailfriend.file.entity.File;
import tf.tailfriend.user.entity.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "boards")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_type_id", nullable = false)
    private BoardType boardType;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder.Default
    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;

    @Builder.Default
    @Column(name = "comment_count", nullable = false)
    private Integer commentCount = 0;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BoardPhoto> photos = new ArrayList<>();

    @OneToOne(mappedBy = "board", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Product product;

    public void addPhoto(File file) {
        BoardPhoto photo = BoardPhoto.of(this, file);
        photos.add(photo);
    }

    public void removePhoto(File file) {
        photos.removeIf(photo -> photo.getFile().equals(file));
    }

    public File removePhotoByFileId(Integer fileId) {
        Iterator<BoardPhoto> iterator = photos.iterator();
        while (iterator.hasNext()) {
            BoardPhoto boardPhoto = iterator.next();
            if (boardPhoto.getFile() != null && boardPhoto.getFile().getId().equals(fileId)) {
                File file = boardPhoto.getFile(); // 반환용
                iterator.remove();
                return file;
            }
        }
        return null;
    }

    public void updateBoard(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public Optional<Product> getProduct() {
        return Optional.ofNullable(product);
    }

    public void increaseCommentCount() {
        commentCount++;
    }

    public void decreaseCommentCount() {
        commentCount--;
    }

    public void increaseLikeCount() {
        likeCount++;
    }

    public void decreaseLikeCount() {
        likeCount--;

    }

}



