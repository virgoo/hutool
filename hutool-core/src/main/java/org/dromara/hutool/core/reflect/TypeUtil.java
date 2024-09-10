/*
 * Copyright (c) 2013-2024 Hutool Team and hutool.cn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.hutool.core.reflect;

import org.dromara.hutool.core.array.ArrayUtil;
import org.dromara.hutool.core.collection.ListUtil;
import org.dromara.hutool.core.util.ObjUtil;

import java.lang.reflect.*;
import java.util.List;
import java.util.Map;

/**
 * 针对 {@link Type} 的工具类封装<br>
 * 最主要功能包括：
 *
 * <pre>
 * 1. 获取方法的参数和返回值类型（包括Type和Class）
 * 2. 获取泛型参数类型（包括对象的泛型参数或集合元素的泛型类型）
 * </pre>
 *
 * @author Looly
 * @since 3.0.8
 */
public class TypeUtil {

	/**
	 * 获得Type对应的原始类
	 *
	 * @param type {@link Type}
	 * @return 原始类，如果无法获取原始类，返回{@code null}
	 */
	public static Class<?> getClass(final Type type) {
		if (null != type) {
			if (type instanceof Class) {
				return (Class<?>) type;
			} else if (type instanceof ParameterizedType) {
				return (Class<?>) ((ParameterizedType) type).getRawType();
			} else if (type instanceof TypeVariable) {
				//return (Class<?>) ((TypeVariable<?>) type).getBounds()[0];
				final Type[] bounds = ((TypeVariable<?>) type).getBounds();
				if (bounds.length == 1) {
					return getClass(bounds[0]);
				}
			} else if (type instanceof WildcardType) {
				final Type[] upperBounds = ((WildcardType) type).getUpperBounds();
				if (upperBounds.length == 1) {
					return getClass(upperBounds[0]);
				}
			} else if(type instanceof GenericArrayType){
				return Array.newInstance(getClass(((GenericArrayType)type).getGenericComponentType()), 0).getClass();
			}
		}
		return null;
	}

	/**
	 * 获取字段对应的Type类型<br>
	 * 方法优先获取GenericType，获取不到则获取Type
	 *
	 * @param field 字段
	 * @return {@link Type}，可能为{@code null}
	 */
	public static Type getType(final Field field) {
		if (null == field) {
			return null;
		}
		return field.getGenericType();
	}

	/**
	 * 获得字段的泛型类型
	 *
	 * @param clazz     Bean类
	 * @param fieldName 字段名
	 * @return 字段的泛型类型
	 * @since 5.4.2
	 */
	public static Type getFieldType(final Class<?> clazz, final String fieldName) {
		return getType(FieldUtil.getField(clazz, fieldName));
	}

	/**
	 * 获得Field对应的原始类
	 *
	 * @param field {@link Field}
	 * @return 原始类，如果无法获取原始类，返回{@code null}
	 * @since 3.1.2
	 */
	public static Class<?> getClass(final Field field) {
		return null == field ? null : field.getType();
	}

	// ----------------------------------------------------------------------------------- Param Type

	/**
	 * 获取方法的第一个参数类型<br>
	 * 优先获取方法的GenericParameterTypes，如果获取不到，则获取ParameterTypes
	 *
	 * @param method 方法
	 * @return {@link Type}，可能为{@code null}
	 * @since 3.1.2
	 */
	public static Type getFirstParamType(final Method method) {
		return getParamType(method, 0);
	}

	/**
	 * 获取方法的第一个参数类
	 *
	 * @param method 方法
	 * @return 第一个参数类型，可能为{@code null}
	 * @since 3.1.2
	 */
	public static Class<?> getFirstParamClass(final Method method) {
		return getParamClass(method, 0);
	}

	/**
	 * 获取方法的参数类型<br>
	 * 优先获取方法的GenericParameterTypes，如果获取不到，则获取ParameterTypes
	 *
	 * @param method 方法
	 * @param index  第几个参数的索引，从0开始计数
	 * @return {@link Type}，可能为{@code null}
	 */
	public static Type getParamType(final Method method, final int index) {
		final Type[] types = getParamTypes(method);
		if (null != types && types.length > index) {
			return types[index];
		}
		return null;
	}

	/**
	 * 获取方法的参数类
	 *
	 * @param method 方法
	 * @param index  第几个参数的索引，从0开始计数
	 * @return 参数类，可能为{@code null}
	 * @since 3.1.2
	 */
	public static Class<?> getParamClass(final Method method, final int index) {
		final Class<?>[] classes = getParamClasses(method);
		if (null != classes && classes.length > index) {
			return classes[index];
		}
		return null;
	}

	/**
	 * 获取方法的参数类型列表<br>
	 * 优先获取方法的GenericParameterTypes，如果获取不到，则获取ParameterTypes
	 *
	 * @param method 方法
	 * @return {@link Type}列表，可能为{@code null}
	 * @see Method#getGenericParameterTypes()
	 * @see Method#getParameterTypes()
	 */
	public static Type[] getParamTypes(final Method method) {
		return null == method ? null : method.getGenericParameterTypes();
	}

	/**
	 * 解析方法的参数类型列表<br>
	 * 依赖jre\lib\rt.jar
	 *
	 * @param method t方法
	 * @return 参数类型类列表
	 * @see Method#getGenericParameterTypes
	 * @see Method#getParameterTypes
	 * @since 3.1.2
	 */
	public static Class<?>[] getParamClasses(final Method method) {
		return null == method ? null : method.getParameterTypes();
	}

	// ----------------------------------------------------------------------------------- Return Type

	/**
	 * 获取方法的返回值类型<br>
	 * 获取方法的GenericReturnType
	 *
	 * @param method 方法
	 * @return {@link Type}，可能为{@code null}
	 * @see Method#getGenericReturnType()
	 * @see Method#getReturnType()
	 */
	public static Type getReturnType(final Method method) {
		return null == method ? null : method.getGenericReturnType();
	}

	/**
	 * 解析方法的返回类型类列表
	 *
	 * @param method 方法
	 * @return 返回值类型的类
	 * @see Method#getGenericReturnType
	 * @see Method#getReturnType
	 * @since 3.1.2
	 */
	public static Class<?> getReturnClass(final Method method) {
		return null == method ? null : method.getReturnType();
	}

	// ----------------------------------------------------------------------------------- Type Argument

	/**
	 * 获得给定类的第一个泛型参数
	 *
	 * @param type 被检查的类型，必须是已经确定泛型类型的类型
	 * @return {@link Type}，可能为{@code null}
	 */
	public static Type getTypeArgument(final Type type) {
		return getTypeArgument(type, 0);
	}

	/**
	 * 获得给定类的泛型参数
	 *
	 * @param type  被检查的类型，必须是已经确定泛型类型的类
	 * @param index 泛型类型的索引号，即第几个泛型类型
	 * @return {@link Type}
	 */
	public static Type getTypeArgument(final Type type, final int index) {
		final Type[] typeArguments = getTypeArguments(type);
		if (null != typeArguments && typeArguments.length > index) {
			return typeArguments[index];
		}
		return null;
	}

	/**
	 * 获得指定类型中所有泛型参数类型，例如：
	 *
	 * <pre>
	 * class A&lt;T&gt;
	 * class B extends A&lt;String&gt;
	 * </pre>
	 * <p>
	 * 通过此方法，传入B.class即可得到String
	 *
	 * @param type 指定类型
	 * @return 所有泛型参数类型
	 */
	public static Type[] getTypeArguments(final Type type) {
		if (null == type) {
			return null;
		}

		final ParameterizedType parameterizedType = toParameterizedType(type);
		return (null == parameterizedType) ? null : parameterizedType.getActualTypeArguments();
	}

	/**
	 * 将{@link Type} 转换为{@link ParameterizedType}<br>
	 * {@link ParameterizedType}用于获取当前类或父类中泛型参数化后的类型<br>
	 * 一般用于获取泛型参数具体的参数类型，例如：
	 *
	 * <pre>
	 * class A&lt;T&gt;
	 * class B extends A&lt;String&gt;
	 * </pre>
	 * <p>
	 * 通过此方法，传入B.class即可得到B{@link ParameterizedType}，从而获取到String
	 *
	 * @param type {@link Type}
	 * @return {@link ParameterizedType}
	 * @since 4.5.2
	 */
	public static ParameterizedType toParameterizedType(final Type type) {
		return toParameterizedType(type, 0);
	}

	/**
	 * 将{@link Type} 转换为{@link ParameterizedType}<br>
	 * {@link ParameterizedType}用于获取当前类或父类中泛型参数化后的类型<br>
	 * 一般用于获取泛型参数具体的参数类型，例如：
	 *
	 * <pre>{@code
	 *   class A<T>
	 *   class B extends A<String>;
	 * }</pre>
	 * <p>
	 * 通过此方法，传入B.class即可得到B对应的{@link ParameterizedType}，从而获取到String
	 *
	 * @param type           {@link Type}
	 * @param interfaceIndex 实现的第几个接口
	 * @return {@link ParameterizedType}
	 * @since 4.5.2
	 */
	public static ParameterizedType toParameterizedType(final Type type, final int interfaceIndex) {
		if (type instanceof ParameterizedType) {
			return (ParameterizedType) type;
		}

		if (type instanceof Class) {
			final ParameterizedType[] generics = getGenerics((Class<?>) type);
			if (generics.length > interfaceIndex) {
				return generics[interfaceIndex];
			}
		}

		return null;
	}

	/**
	 * 获取指定类所有泛型父类和泛型接口
	 * <ul>
	 *     <li>指定类及其所有的泛型父类</li>
	 *     <li>指定类实现的直接泛型接口</li>
	 * </ul>
	 *
	 * @param clazz 类
	 * @return 泛型父类或接口数组
	 * @since 6.0.0
	 */
	public static ParameterizedType[] getGenerics(final Class<?> clazz) {
		final List<ParameterizedType> result = ListUtil.of(false);
		// 泛型父类（父类及祖类优先级高）
		final Type genericSuper = clazz.getGenericSuperclass();
		if (null != genericSuper && !Object.class.equals(genericSuper)) {
			final ParameterizedType parameterizedType = toParameterizedType(genericSuper);
			if (null != parameterizedType) {
				result.add(parameterizedType);
			}
		}

		// 泛型接口
		final Type[] genericInterfaces = clazz.getGenericInterfaces();
		if (ArrayUtil.isNotEmpty(genericInterfaces)) {
			for (final Type genericInterface : genericInterfaces) {
				final ParameterizedType parameterizedType = toParameterizedType(genericInterface);
				if(null != parameterizedType){
					result.add(parameterizedType);
				}
			}
		}
		return result.toArray(new ParameterizedType[0]);
	}

	/**
	 * 是否未知类型<br>
	 * type为null或者{@link TypeVariable} 都视为未知类型
	 *
	 * @param type Type类型
	 * @return 是否未知类型
	 * @since 4.5.2
	 */
	public static boolean isUnknown(final Type type) {
		return null == type || type instanceof TypeVariable;
	}

	/**
	 * 指定泛型数组中是否含有泛型变量
	 *
	 * @param types 泛型数组
	 * @return 是否含有泛型变量
	 * @since 4.5.7
	 */
	public static boolean hasTypeVariable(final Type... types) {
		for (final Type type : types) {
			if (type instanceof TypeVariable) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 获取泛型变量和泛型实际类型的对应关系Map，例如：
	 *
	 * <pre>
	 *     T    org.dromara.hutool.test.User
	 *     E    java.lang.Integer
	 * </pre>
	 *
	 * @param clazz 被解析的包含泛型参数的类
	 * @return 泛型对应关系Map
	 */
	public static Map<Type, Type> getTypeMap(final Class<?> clazz) {
		return ActualTypeMapperPool.get(clazz);
	}

	/**
	 * 获得泛型字段对应的泛型实际类型，如果此变量没有对应的实际类型，返回null
	 *
	 * @param type  实际类型明确的类
	 * @param field 字段
	 * @return 实际类型，可能为Class等
	 */
	public static Type getActualType(final Type type, final Field field) {
		if (null == field) {
			return null;
		}
		return getActualType(ObjUtil.defaultIfNull(type, field.getDeclaringClass()), field.getGenericType());
	}

	/**
	 * 获得泛型变量对应的泛型实际类型，如果此变量没有对应的实际类型，返回null
	 * 此方法可以处理：
	 *
	 * <pre>
	 *     1. 泛型化对象，类似于Map&lt;User, Key&lt;Long&gt;&gt;
	 *     2. 泛型变量，类似于T
	 * </pre>
	 *
	 * @param type         类
	 * @param typeVariable 泛型变量，例如T等
	 * @return 实际类型，可能为Class等
	 */
	public static Type getActualType(final Type type, final Type typeVariable) {
		if (typeVariable instanceof ParameterizedType) {
			return getActualType(type, (ParameterizedType) typeVariable);
		}

		if (typeVariable instanceof TypeVariable) {
			return ActualTypeMapperPool.getActualType(type, (TypeVariable<?>) typeVariable);
		}

		// 没有需要替换的泛型变量，原样输出
		return typeVariable;
	}

	/**
	 * 获得泛型变量对应的泛型实际类型，如果此变量没有对应的实际类型，返回null
	 * 此方法可以处理复杂的泛型化对象，类似于Map&lt;User, Key&lt;Long&gt;&gt;
	 *
	 * @param type              类
	 * @param parameterizedType 泛型变量，例如List&lt;T&gt;等
	 * @return 实际类型，可能为Class等
	 */
	public static Type getActualType(final Type type, ParameterizedType parameterizedType) {
		// 字段类型为泛型参数类型，解析对应泛型类型为真实类型，类似于List<T> a
		Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

		// 泛型对象中含有未被转换的泛型变量
		if (TypeUtil.hasTypeVariable(actualTypeArguments)) {
			actualTypeArguments = getActualTypes(type, parameterizedType.getActualTypeArguments());
			if (ArrayUtil.isNotEmpty(actualTypeArguments)) {
				// 替换泛型变量为实际类型，例如List<T>变为List<String>
				parameterizedType = new ParameterizedTypeImpl(actualTypeArguments, parameterizedType.getOwnerType(), parameterizedType.getRawType());
			}
		}

		return parameterizedType;
	}

	/**
	 * 获得泛型变量对应的泛型实际类型，如果此变量没有对应的实际类型，返回null
	 *
	 * @param type          类
	 * @param typeVariables 泛型变量数组，例如T等
	 * @return 实际类型数组，可能为Class等
	 */
	public static Type[] getActualTypes(final Type type, final Type... typeVariables) {
		return ActualTypeMapperPool.getActualTypes(type, typeVariables);
	}
}
