package tf.tailfriend.chat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
    @Builder.Default
    private List<Message> messages = new ArrayList<>();

    public static Message createMessage(ChatRoom chatRoom, User user, String content, ChatType chatType, String metadata) {
        return Message.builder()
                .chatRoom(chatRoom)
                .user(user)
                .content(content)
                .chatType(chatType)
                .metadata(metadata)
                .build();
    }

    public void addMessage(User user, String content, ChatType chatType, String metadata) {
        Message message = createMessage(this, user, content, chatType, metadata);
        messages.add(message);
        this.lastUpdate = LocalDateTime.now();
    }
}
