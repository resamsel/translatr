package utils;

import com.avaje.ebean.PagedList;
import com.google.common.base.CaseFormat;
import dto.Dto;
import dto.DtoPagedList;
import org.reflections.Reflections;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DtoGenerator {

  private static final Set<String> IGNORED_FIELDS = new HashSet<>(Arrays.asList("serialVersionUID", "LOGGER"));
  private static final String TARGET_DIR = "ui/libs/translatr-model/src/lib/model-generated";
  private final File targetDir;
  private final String packageName;

  private DtoGenerator(String targetDir, String packageName) {
    this.targetDir = new File(targetDir);
    this.packageName = packageName;
  }

  public static void main(String[] args) throws IOException {
    DtoGenerator generator = new DtoGenerator(TARGET_DIR, "dto");

    generator.generate();
  }

  private void generate() throws IOException {
    if (!targetDir.exists()) {
      targetDir.mkdirs();
    }

    Reflections reflections = new Reflections(packageName);

    write("paged-list.ts", "export interface PagedList<T> {\n" +
        "  list: Array<T>;\n" +
        "  hasNext: boolean;\n" +
        "  hasPrev: boolean;\n" +
        "  limit: number;\n" +
        "  offset: number;\n" +
        "}\n");

    Set<Class<?>> classes = reflections.getSubTypesOf(Dto.class)
        .stream()
        .flatMap(this::includeDependencies)
        .filter(dtoClass -> dtoClass != Dto.class)
        .filter(dtoClass -> dtoClass != DtoPagedList.class)
        .filter(dtoClass -> dtoClass.getPackage() != null && dtoClass.getPackage().getName().startsWith(packageName))
        .filter(dtoClass -> dtoClass.getSuperclass() != DtoPagedList.class)
        .filter(dtoClass -> dtoClass.getSuperclass() != PagedList.class)
        .collect(Collectors.toSet());

    classes.forEach(dtoClass -> {
      try {
        handleDto(dtoClass);
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  private Stream<? extends Class<?>> includeDependencies(Class<? extends Dto> dtoClass) {
    return Stream.concat(
        Arrays.stream(new Class<?>[]{dtoClass, dtoClass.getSuperclass()})
            .filter(Objects::nonNull),
        Arrays.stream(dtoClass.getDeclaredFields()).map(Field::getType));
  }

  private void write(String fileName, String contents) throws IOException {
    try (FileWriter writer = new FileWriter(new File(targetDir, fileName))) {
      writer.write(contents);
    } catch (IOException e) {
      e.printStackTrace();
      throw e;
    }
  }

  private void handleDto(Class<?> dtoClass) throws IOException {
    String simpleName = dtoClass.getSimpleName();
    String contents = imports(dtoClass) + "\n\n";

    if (dtoClass.isEnum()) {
      contents += "export enum " + simpleName + " {\n" +
          "  " +
          Arrays.stream(dtoClass.getDeclaredFields())
              .filter(Field::isEnumConstant)
              .map(Field::getName)
              .collect(Collectors.joining(", ")) +
          "\n}\n";
    } else {
      contents += "export interface " + simpleName + extension(dtoClass) + " {\n" +
          fields(dtoClass) +
          "\n}\n";
    }

    write(fileName(simpleName), contents);
  }

  private String fileName(String simpleName) {
    return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, simpleName) + ".ts";
  }

  private String imports(Class<?> dtoClass) {
    return Stream.concat(
        Arrays.stream(dtoClass.getDeclaredFields())
            .filter(field -> !field.isEnumConstant())
            .map(Field::getType),
        Stream.of(dtoClass.getSuperclass())
            .filter(Objects::nonNull)
            .filter(clazz -> clazz != Object.class)
            .filter(clazz -> clazz != Dto.class))
        .filter(clazz -> clazz.getPackage() != null)
        .filter(clazz -> clazz.getPackage().getName().startsWith(packageName))
        .distinct()
        .map(clazz -> String.format(
            "import { %s } from './%s';",
            clazz.getSimpleName(),
            fileName(clazz.getSimpleName()).replaceAll("\\.ts", "")))
        .collect(Collectors.joining("\n"));
  }

  private static String extension(Class<?> dtoClass) {
    if (dtoClass.isEnum() || dtoClass.getSuperclass() == Object.class) {
      return "";
    }

    if (dtoClass.getSuperclass() != null && dtoClass.getSuperclass() != Dto.class) {
      return " extends " + toTypeScriptType(dtoClass.getSuperclass());
    }

    return "";
  }

  private static String fields(Class<?> dtoClass) {
    return Arrays.stream(dtoClass.getDeclaredFields())
        .filter(field -> !IGNORED_FIELDS.contains(field.getName()))
        .map(field -> String.format("  %1$s?: %2$s;", field.getName(), toTypeScriptType(field)))
        .collect(Collectors.joining("\n"));
  }

  private static String getParameterizedType(Field field) {
    if (field.getGenericType() instanceof ParameterizedType) {
      ParameterizedType type = (ParameterizedType) field.getGenericType();
      return type.getActualTypeArguments()[0].getTypeName();
    }
    return null;
  }

  private static String toTypeScriptType(Field field) {
    System.out.println(field);
    Class<?> type = field.getType();

    String parameterizedType = getParameterizedType(field);
    if (parameterizedType != null) {
      return String.format("Array<%s>", toTypeScriptType(parameterizedType));
    }

    return toTypeScriptType(type);
  }

  private static String toTypeScriptType(Class<?> type) {
    return toTypeScriptType(type.getSimpleName());
  }

  private static String toTypeScriptType(String typeName) {
    String trimmedTypeName = typeName.replaceAll("[a-z]+\\.", "");
    switch (trimmedTypeName) {
      case "String":
      case "UUID":
        return "string";
      case "long":
      case "Long":
      case "int":
      case "Integer":
        return "number";
      case "DateTime":
        return "Date";
      case "DtoPagedList":
        return "PagedList";
      case "List":
        return "Array";
      default:
        System.out.println(typeName);
        return trimmedTypeName;
    }
  }
}