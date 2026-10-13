package syy.portPilotage.dto.common;

import lombok.Data;

import java.util.Map;

@Data
public class PageQueryDTO {
    
    private int current = 1;
    private int size = 10;
    private String orderBy;
    private Boolean asc = true;
    private String keyword;
    private Map<String, Object> params;
    
        private Integer page;
    private Integer pageSize;
    
    public void setPage(Integer page) {
        this.page = page;
        if (page != null) {
            this.current = page;
        }
    }
    
    public Integer getPage() {
        return page != null ? page : current;
    }
    
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        if (pageSize != null) {
            this.size = pageSize;
        }
    }
    
    public Integer getPageSize() {
        return pageSize != null ? pageSize : size;
    }
}