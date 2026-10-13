package syy.portPilotage.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;
import syy.portPilotage.enums.WeatherConditionEnum;

/**
 * 气象信息表
 * @TableName weather_info
 */
@TableName(value ="weather_info")
@Data
public class WeatherInfo implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 气象日期
     */
    @TableField(value = "weather_date")
    private Date weatherDate;

    /**
     * 气象次别
     */
    @TableField(value = "weather_sequence")
    private Integer weatherSequence;

    /**
     * 气象内容
     */
    @TableField(value = "weather_content")
    private String weatherContent;

    /**
     * 温度
     */
    @TableField(value = "temperature")
    private BigDecimal temperature;

    /**
     * 湿度
     */
    @TableField(value = "humidity")
    private BigDecimal humidity;

    /**
     * 风速
     */
    @TableField(value = "wind_speed")
    private BigDecimal windSpeed;

    /**
     * 风向
     */
    @TableField(value = "wind_direction")
    private String windDirection;

    /**
     * 能见度
     */
    @TableField(value = "visibility")
    private BigDecimal visibility;

    /**
     * 气压
     */
    @TableField(value = "pressure")
    private BigDecimal pressure;

    /**
     * 天气状况
     */
    @TableField(value = "weather_condition")
    private WeatherConditionEnum weatherCondition;

    /**
     * 数据来源
     */
    @TableField(value = "data_source")
    private String dataSource;

        @TableField(value = "created_at")
    private Date createdAt;

        @TableField(value = "updated_at")
    private Date updatedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        WeatherInfo other = (WeatherInfo) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getWeatherDate() == null ? other.getWeatherDate() == null : this.getWeatherDate().equals(other.getWeatherDate()))
            && (this.getWeatherSequence() == null ? other.getWeatherSequence() == null : this.getWeatherSequence().equals(other.getWeatherSequence()))
            && (this.getWeatherContent() == null ? other.getWeatherContent() == null : this.getWeatherContent().equals(other.getWeatherContent()))
            && (this.getTemperature() == null ? other.getTemperature() == null : this.getTemperature().equals(other.getTemperature()))
            && (this.getHumidity() == null ? other.getHumidity() == null : this.getHumidity().equals(other.getHumidity()))
            && (this.getWindSpeed() == null ? other.getWindSpeed() == null : this.getWindSpeed().equals(other.getWindSpeed()))
            && (this.getWindDirection() == null ? other.getWindDirection() == null : this.getWindDirection().equals(other.getWindDirection()))
            && (this.getVisibility() == null ? other.getVisibility() == null : this.getVisibility().equals(other.getVisibility()))
            && (this.getPressure() == null ? other.getPressure() == null : this.getPressure().equals(other.getPressure()))
            && (this.getWeatherCondition() == null ? other.getWeatherCondition() == null : this.getWeatherCondition().equals(other.getWeatherCondition()))
            && (this.getDataSource() == null ? other.getDataSource() == null : this.getDataSource().equals(other.getDataSource()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getWeatherDate() == null) ? 0 : getWeatherDate().hashCode());
        result = prime * result + ((getWeatherSequence() == null) ? 0 : getWeatherSequence().hashCode());
        result = prime * result + ((getWeatherContent() == null) ? 0 : getWeatherContent().hashCode());
        result = prime * result + ((getTemperature() == null) ? 0 : getTemperature().hashCode());
        result = prime * result + ((getHumidity() == null) ? 0 : getHumidity().hashCode());
        result = prime * result + ((getWindSpeed() == null) ? 0 : getWindSpeed().hashCode());
        result = prime * result + ((getWindDirection() == null) ? 0 : getWindDirection().hashCode());
        result = prime * result + ((getVisibility() == null) ? 0 : getVisibility().hashCode());
        result = prime * result + ((getPressure() == null) ? 0 : getPressure().hashCode());
        result = prime * result + ((getWeatherCondition() == null) ? 0 : getWeatherCondition().hashCode());
        result = prime * result + ((getDataSource() == null) ? 0 : getDataSource().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        result = prime * result + ((getUpdatedAt() == null) ? 0 : getUpdatedAt().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", weatherDate=").append(weatherDate);
        sb.append(", weatherSequence=").append(weatherSequence);
        sb.append(", weatherContent=").append(weatherContent);
        sb.append(", temperature=").append(temperature);
        sb.append(", humidity=").append(humidity);
        sb.append(", windSpeed=").append(windSpeed);
        sb.append(", windDirection=").append(windDirection);
        sb.append(", visibility=").append(visibility);
        sb.append(", pressure=").append(pressure);
        sb.append(", weatherCondition=").append(weatherCondition);
        sb.append(", dataSource=").append(dataSource);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}