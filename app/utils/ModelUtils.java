package utils;

import com.avaje.ebean.PagedList;
import com.google.common.base.CaseFormat;
import dto.Dto;
import dto.DtoPagedList;
import org.reflections.Reflections;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ModelUtils {

  private static final Set<String> IGNORED_FIELDS = new HashSet<>(Arrays.asList("serialVersionUID"));

  public static void main(String[] args) {
    Reflections reflections = new Reflections("dto");

    System.out.println("paged-list.ts");
    System.out.println("export interface PagedList<T> {\n" +
        "  list: Array<T>;\n" +
        "  hasNext: boolean;\n" +
        "  hasPrev: boolean;\n" +
        "  limit: number;\n" +
        "  offset: number;\n" +
        "}\n");

    reflections.getSubTypesOf(Dto.class)
        .stream()
        .filter(dtoClass -> dtoClass.getSuperclass() != DtoPagedList.class)
        .filter(dtoClass -> dtoClass.getSuperclass() != PagedList.class)
        .forEach(ModelUtils::handleDto);
  }

  private static void handleDto(Class<? extends Dto> dtoClass) {
    String simpleName = dtoClass.getSimpleName();
    String fileName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, simpleName) + ".ts";

    String contents = "export interface " + simpleName + extension(dtoClass) +" {\n" +
        fields(dtoClass) +
        "\n}\n";

    System.out.println(fileName);
    System.out.println(contents);
  }

  private static String extension(Class<? extends Dto> dtoClass) {
    if (dtoClass.getSuperclass() != null && dtoClass.getSuperclass() != Dto.class) {
      return " extends " + toTypeScriptType(dtoClass.getSuperclass());
    }

    return "";
  }

  private static String fields(Class<? extends Dto> dtoClass) {
    return Arrays.stream(dtoClass.getDeclaredFields())
        .filter(field -> !IGNORED_FIELDS.contains(field.getName()))
        .map(field -> String.format("  %1$s?: %2$s;", field.getName(), toTypeScriptType(field.getType())))
        .collect(Collectors.joining("\n"));
  }

  private static String toTypeScriptType(Class<?> type) {
    switch (type.getSimpleName()) {
      case "String":
      case "UUID":
        return "string";
      case "long":
      case "int":
        return "number";
      case "DateTime":
        return "Date";
      case "DtoPagedList":
        return "PagedList";
    }
    return type.getSimpleName();
  }
}
