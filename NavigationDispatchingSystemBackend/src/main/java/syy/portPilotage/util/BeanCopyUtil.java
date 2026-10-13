package syy.portPilotage.util;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class BeanCopyUtil {
    
    public static <S, T> T copy(S source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("Bean拷贝失败", e);
        }
    }
    public static void copyPropertiesIgnoreNull(Object source, Object target) {
        BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
    }

    private static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) {
                emptyNames.add(pd.getName());
            }
        }
        return emptyNames.toArray(new String[0]);
    }
    public static <S, T> T copy(S source, Supplier<T> targetSupplier) {
        if (source == null) {
            return null;
        }
        T target = targetSupplier.get();
        BeanUtils.copyProperties(source, target);
        return target;
    }
    
    public static <S, T> List<T> copyList(List<S> sourceList, Class<T> targetClass) {
        if (sourceList == null || sourceList.isEmpty()) {
            return new ArrayList<>();
        }
        List<T> targetList = new ArrayList<>(sourceList.size());
        for (S source : sourceList) {
            T target = copy(source, targetClass);
            targetList.add(target);
        }
        return targetList;
    }
    
    public static <S, T> List<T> copyList(List<S> sourceList, Supplier<T> targetSupplier) {
        if (sourceList == null || sourceList.isEmpty()) {
            return new ArrayList<>();
        }
        List<T> targetList = new ArrayList<>(sourceList.size());
        for (S source : sourceList) {
            T target = copy(source, targetSupplier);
            targetList.add(target);
        }
        return targetList;
    }
}