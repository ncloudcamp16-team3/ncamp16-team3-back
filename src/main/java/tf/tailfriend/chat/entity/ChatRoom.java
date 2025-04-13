package tf.tailfriend.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import tf.tailfriend.user.entity.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chat_rooms")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id1", nullable = false)
    private User user1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id2", nullable = false)
    private User user2;

    @CreationTimestamp
    @Column(name = "last_update", nullable = false)
    private LocalDateTime lastUpdate;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
    private List<Message> messages = new ArrayList<>();

    public void addMessage(Message message) {
        messages.add(message);
        message.setChatRoom(this);
        this.lastUpdate = LocalDateTime.now();
    }
}
