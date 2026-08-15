package com.spilledsoup.umapi.content.runtime;

import com.spilledsoup.umapi.content.ContentRegistry;
import com.spilledsoup.umapi.content.ItemContent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ReflectiveItemRegistrar {
    private static final String FORGE_REGISTER_EVENT = "net.minecraftforge.registries.RegisterEvent";
    private static final String NEOFORGE_REGISTER_EVENT = "net.neoforged.neoforge.registries.RegisterEvent";
    private static final String OLD_FORGE_REGISTER_EVENT = "net.minecraftforge.event.RegistryEvent$Register";

    private ReflectiveItemRegistrar() {
    }

    public static void register(ContentRegistry content, Object context) {
        Objects.requireNonNull(content, "content");

        if (content.items().isEmpty()) {
            return;
        }

        if (context != null) {
            registerWithEventBus(content, context);
            return;
        }

        content.items().forEach(ReflectiveItemRegistrar::registerDirect);
    }

    private static void registerWithEventBus(ContentRegistry content, Object eventBus) {
        if (classExists(FORGE_REGISTER_EVENT)) {
            registerWithRegisterEvent(content, eventBus, FORGE_REGISTER_EVENT);
            return;
        }

        if (classExists(NEOFORGE_REGISTER_EVENT)) {
            registerWithRegisterEvent(content, eventBus, NEOFORGE_REGISTER_EVENT);
            return;
        }

        if (classExists(OLD_FORGE_REGISTER_EVENT)) {
            registerWithOldForgeRegisterEvent(content, eventBus);
            return;
        }

        throw new IllegalStateException(
                "Could not find a supported Forge-family item registration event."
        );
    }

    private static void registerWithRegisterEvent(
            ContentRegistry content,
            Object eventBus,
            String registerEventClassName
    ) {
        Class<?> registerEventClass = requiredClass(registerEventClassName);
        Object itemRegistryKey = itemRegistryKey();

        Consumer<Object> listener = event -> {
            if (!itemRegistryKey.equals(invoke(event, "getRegistryKey"))) {
                return;
            }

            for (ItemContent item : content.items()) {
                registerWithEvent(event, itemRegistryKey, item);
            }
        };

        addTypedEventListener(eventBus, registerEventClass, listener);
    }

    private static void registerWithOldForgeRegisterEvent(ContentRegistry content, Object eventBus) {
        Consumer<Object> listener = event -> {
            Object registry = invoke(event, "getRegistry");

            for (ItemContent item : content.items()) {
                invoke(registry, "register", createItem(item, true));
            }
        };

        addGenericEventListener(eventBus, itemClass(), listener);
    }

    private static void registerWithEvent(Object event, Object itemRegistryKey, ItemContent item) {
        Object id = resourceId(item.namespace(), item.id());
        Supplier<Object> supplier = () -> createItem(item, false);

        Method register = findCompatibleMethod(
                event.getClass(),
                "register",
                itemRegistryKey,
                id,
                supplier
        );

        invokeMethod(register, event, itemRegistryKey, id, supplier);
    }

    private static void registerDirect(ItemContent item) {
        Object registry = itemRegistry();
        Object id = resourceId(item.namespace(), item.id());
        Object minecraftItem = createItem(item, false);

        Method register = findCompatibleStaticMethod(
                requiredClass("net.minecraft.core.Registry"),
                "register",
                registry,
                id,
                minecraftItem
        );

        invokeMethod(register, null, registry, id, minecraftItem);
    }

    private static Object createItem(ItemContent item, boolean setRegistryName) {
        try {
            Class<?> itemClass = itemClass();
            Class<?> propertiesClass = requiredClass(itemClass.getName() + "$Properties");
            Object properties = propertiesClass.getConstructor().newInstance();

            setItemIdIfSupported(properties, item);

            Object minecraftItem = itemClass.getConstructor(propertiesClass)
                    .newInstance(properties);

            if (setRegistryName) {
                Object id = resourceId(item.namespace(), item.id());
                invoke(minecraftItem, "setRegistryName", id);
            }

            return minecraftItem;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(
                    "Could not create item " + item.qualifiedId() + ".",
                    exception
            );
        }
    }

    private static void setItemIdIfSupported(Object properties, ItemContent item) {
        Method setId = findMethodOrNull(properties.getClass(), "setId", 1);
        if (setId == null) {
            return;
        }

        Object id = resourceId(item.namespace(), item.id());
        Object registryKey = itemRegistryKey();
        Object itemKey = createResourceKey(registryKey, id);
        invokeMethod(setId, properties, itemKey);
    }

    private static Object itemRegistry() {
        Class<?> builtInRegistries = findClassOrNull("net.minecraft.core.registries.BuiltInRegistries");
        if (builtInRegistries != null) {
            return fieldValue(builtInRegistries, "ITEM");
        }

        return fieldValue(requiredClass("net.minecraft.core.Registry"), "ITEM");
    }

    private static Object itemRegistryKey() {
        Class<?> registries = findClassOrNull("net.minecraft.core.registries.Registries");
        if (registries != null) {
            return fieldValue(registries, "ITEM");
        }

        return fieldValue(requiredClass("net.minecraft.core.Registry"), "ITEM_REGISTRY");
    }

    private static Object createResourceKey(Object registryKey, Object id) {
        Class<?> resourceKeyClass = requiredClass("net.minecraft.resources.ResourceKey");
        Method create = findCompatibleStaticMethod(resourceKeyClass, "create", registryKey, id);
        return invokeMethod(create, null, registryKey, id);
    }

    private static Object resourceId(String namespace, String path) {
        Class<?> idClass = findClass(
                "net.minecraft.resources.Identifier",
                "net.minecraft.resources.ResourceLocation",
                "net.minecraft.util.ResourceLocation"
        );

        Method factory = findStaticMethodOrNull(idClass, "fromNamespaceAndPath", 2);
        if (factory != null) {
            return invokeMethod(factory, null, namespace, path);
        }

        try {
            return idClass.getConstructor(String.class, String.class)
                    .newInstance(namespace, path);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(
                    "Could not create resource id " + namespace + ":" + path + ".",
                    exception
            );
        }
    }

    private static Class<?> itemClass() {
        return findClass(
                "net.minecraft.world.item.Item",
                "net.minecraft.item.Item"
        );
    }

    private static void addTypedEventListener(
            Object eventBus,
            Class<?> eventClass,
            Consumer<Object> listener
    ) {
        Method method = findEventBusAddListener(eventBus.getClass(), eventClass);
        Object[] arguments = eventBusArguments(method, eventClass, listener);
        invokeMethod(method, eventBus, arguments);
    }

    private static void addGenericEventListener(
            Object eventBus,
            Class<?> genericType,
            Consumer<Object> listener
    ) {
        Method method = findMethod(eventBus.getClass(), "addGenericListener", 2);
        invokeMethod(method, eventBus, genericType, listener);
    }

    private static Method findEventBusAddListener(Class<?> eventBusClass, Class<?> eventClass) {
        for (Method method : eventBusClass.getMethods()) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (!"addListener".equals(method.getName())) {
                continue;
            }

            if (parameterTypes.length == 4
                    && parameterTypes[1] == boolean.class
                    && parameterTypes[2] == Class.class
                    && Consumer.class.isAssignableFrom(parameterTypes[3])) {
                return method;
            }

            if (parameterTypes.length == 3
                    && parameterTypes[1] == Class.class
                    && Consumer.class.isAssignableFrom(parameterTypes[2])) {
                return method;
            }

            if (parameterTypes.length == 2
                    && parameterTypes[0] == Class.class
                    && Consumer.class.isAssignableFrom(parameterTypes[1])
                    && parameterTypes[0].isInstance(eventClass)) {
                return method;
            }
        }

        throw new IllegalStateException("Could not find IEventBus.addListener overload.");
    }

    private static Object[] eventBusArguments(
            Method method,
            Class<?> eventClass,
            Consumer<Object> listener
    ) {
        Class<?>[] parameterTypes = method.getParameterTypes();

        if (parameterTypes.length == 4) {
            return new Object[]{enumConstant(parameterTypes[0], "NORMAL"), false, eventClass, listener};
        }

        if (parameterTypes.length == 3) {
            return new Object[]{enumConstant(parameterTypes[0], "NORMAL"), eventClass, listener};
        }

        return new Object[]{eventClass, listener};
    }

    private static Object enumConstant(Class<?> enumClass, String name) {
        if (!enumClass.isEnum()) {
            throw new IllegalStateException("Expected enum type, got " + enumClass.getName() + ".");
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        Object value = Enum.valueOf((Class<? extends Enum>) enumClass.asSubclass(Enum.class), name);
        return value;
    }

    private static Method findCompatibleStaticMethod(
            Class<?> targetClass,
            String methodName,
            Object... arguments
    ) {
        for (Method method : targetClass.getMethods()) {
            if (!Modifier.isStatic(method.getModifiers())) {
                continue;
            }

            if (isCompatible(method, methodName, arguments)) {
                return method;
            }
        }

        throw new IllegalStateException(
                "Could not find compatible static method " + targetClass.getName() + "." + methodName + "."
        );
    }

    private static Method findCompatibleMethod(
            Class<?> targetClass,
            String methodName,
            Object... arguments
    ) {
        for (Method method : targetClass.getMethods()) {
            if (isCompatible(method, methodName, arguments)) {
                return method;
            }
        }

        throw new IllegalStateException(
                "Could not find compatible method " + targetClass.getName() + "." + methodName + "."
        );
    }

    private static boolean isCompatible(Method method, String methodName, Object... arguments) {
        if (!methodName.equals(method.getName())) {
            return false;
        }

        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != arguments.length) {
            return false;
        }

        for (int index = 0; index < parameterTypes.length; index++) {
            if (arguments[index] != null && !parameterTypes[index].isInstance(arguments[index])) {
                return false;
            }
        }

        return true;
    }

    private static Method findMethod(Class<?> targetClass, String methodName, int parameterCount) {
        Method method = findMethodOrNull(targetClass, methodName, parameterCount);
        if (method == null) {
            throw new IllegalStateException(
                    "Could not find method " + targetClass.getName() + "." + methodName + "."
            );
        }

        return method;
    }

    private static Method findMethodOrNull(Class<?> targetClass, String methodName, int parameterCount) {
        for (Method method : targetClass.getMethods()) {
            if (methodName.equals(method.getName())
                    && method.getParameterTypes().length == parameterCount) {
                return method;
            }
        }

        return null;
    }

    private static Method findStaticMethodOrNull(Class<?> targetClass, String methodName, int parameterCount) {
        for (Method method : targetClass.getMethods()) {
            if (Modifier.isStatic(method.getModifiers())
                    && methodName.equals(method.getName())
                    && method.getParameterTypes().length == parameterCount) {
                return method;
            }
        }

        return null;
    }

    private static Object fieldValue(Class<?> targetClass, String name) {
        try {
            return targetClass.getField(name).get(null);
        } catch (IllegalAccessException | NoSuchFieldException exception) {
            throw new IllegalStateException(
                    "Could not read field " + targetClass.getName() + "." + name + ".",
                    exception
            );
        }
    }

    private static Object invoke(Object target, String methodName, Object... arguments) {
        Method method = findCompatibleMethod(target.getClass(), methodName, arguments);
        return invokeMethod(method, target, arguments);
    }

    private static Object invokeMethod(Method method, Object target, Object... arguments) {
        try {
            return method.invoke(target, arguments);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException(
                    "Could not invoke " + method.getDeclaringClass().getName() + "." + method.getName() + ".",
                    exception
            );
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }

            throw new IllegalStateException(
                    "Could not invoke " + method.getDeclaringClass().getName() + "." + method.getName() + ".",
                    cause
            );
        }
    }

    private static boolean classExists(String className) {
        return findClassOrNull(className) != null;
    }

    private static Class<?> requiredClass(String className) {
        Class<?> result = findClassOrNull(className);
        if (result == null) {
            throw new IllegalStateException("Could not find class " + className + ".");
        }

        return result;
    }

    private static Class<?> findClass(String... classNames) {
        for (String className : classNames) {
            Class<?> result = findClassOrNull(className);
            if (result != null) {
                return result;
            }
        }

        throw new IllegalStateException("Could not find any supported Minecraft class.");
    }

    private static Class<?> findClassOrNull(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ignored) {
            String mappedClassName = mapClassName(className);
            if (mappedClassName == null || mappedClassName.equals(className)) {
                return null;
            }

            try {
                return Class.forName(mappedClassName);
            } catch (ClassNotFoundException ignoredAgain) {
                return null;
            }
        }
    }

    private static String mapClassName(String officialClassName) {
        String mapped = mapFabricClassName(officialClassName);
        if (mapped != null) {
            return mapped;
        }

        return mapQuiltClassName(officialClassName);
    }

    private static String mapFabricClassName(String officialClassName) {
        try {
            Class<?> loaderClass = Class.forName("net.fabricmc.loader.api.FabricLoader");
            Object loader = invokeMethod(loaderClass.getMethod("getInstance"), null);
            Object resolver = invoke(loader, "getMappingResolver");
            return (String) invoke(resolver, "mapClassName", "official", officialClassName);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }

    private static String mapQuiltClassName(String officialClassName) {
        try {
            Class<?> loaderClass = Class.forName("org.quiltmc.loader.api.QuiltLoader");
            Object resolver = invokeMethod(loaderClass.getMethod("getMappingResolver"), null);
            return (String) invoke(resolver, "mapClassName", "official", officialClassName);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }
}
