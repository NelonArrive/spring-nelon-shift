package nelon.arrive.nelonshift.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.time.Instant;
import java.util.UUID;

@RedisHash("refreshToken")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {
	@Id
	private String id;
	
	@Indexed
	private UUID userId;
	
	private String token;
	
	private Instant createdAt;
	
	private Instant expireAt;
	
	@TimeToLive
	private Long ttl;
	
	public boolean isExpired() {
		return Instant.now().isAfter(expireAt);
	}
}
