package syy.portPilotage.security.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPermission implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long userId;
    private List<String> roles;
    private List<String> permissions;
}
