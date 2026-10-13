package syy.portPilotage.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private long total;
    private long current;
    private long size;
    private long pages;
    private List<T> records;
    
    public PageResult(long total, long current, long size, List<T> records) {
        this.total = total;
        this.current = current;
        this.size = size;
        this.pages = size > 0 ? (total + size - 1) / size : 0;
        this.records = records;
    }
    
    public static <T> PageResult<T> of(long total, long current, long size, List<T> records) {
        return new PageResult<>(total, current, size, records);
    }
}