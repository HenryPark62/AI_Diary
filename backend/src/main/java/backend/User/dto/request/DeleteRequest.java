package backend.User.dto.request;

import backend.User.entity.DeleteReason;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteRequest {
    private DeleteReason deleteReason;
    private String deleteReason_Description;
}
