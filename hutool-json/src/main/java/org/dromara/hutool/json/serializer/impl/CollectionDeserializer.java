/*
 * Copyright (c) 2024 Hutool Team and hutool.cn
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

package org.dromara.hutool.json.serializer.impl;

import org.dromara.hutool.core.collection.CollUtil;
import org.dromara.hutool.core.reflect.TypeUtil;
import org.dromara.hutool.json.JSON;
import org.dromara.hutool.json.JSONArray;
import org.dromara.hutool.json.JSONObject;
import org.dromara.hutool.json.serializer.MatcherJSONDeserializer;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/**
 * 集合类型反序列化器
 *
 * @author looly
 * @since 6.0.0
 */
public class CollectionDeserializer implements MatcherJSONDeserializer<Collection<?>> {

	/**
	 * 单例
	 */
	public static final CollectionDeserializer INSTANCE = new CollectionDeserializer();

	@Override
	public boolean match(final JSON json, final Type deserializeType) {
		if (json instanceof JSONArray || json instanceof JSONObject) {
			final Class<?> rawType = TypeUtil.getClass(deserializeType);
			return Collection.class.isAssignableFrom(rawType);
		}
		return false;
	}

	@Override
	public Collection<?> deserialize(final JSON json, final Type deserializeType) {
		final Class<?> rawType = TypeUtil.getClass(deserializeType);
		final Type elementType = TypeUtil.getTypeArgument(deserializeType);
		final Collection<?> result = CollUtil.create(rawType, TypeUtil.getClass(elementType));


		if (json instanceof JSONObject) {
			fill((JSONObject) json, result, elementType);
		} else {
			fill((JSONArray) json, result, elementType);
		}

		return result;
	}

	/**
	 * 将JSONObject转换为集合
	 *
	 * @param json        JSONObject
	 * @param result      结果集合
	 * @param elementType 元素类型
	 */
	private void fill(final JSONObject json, final Collection<?> result, final Type elementType) {
		for (final Map.Entry<String, JSON> entry : json) {
			result.add(entry.getValue().toBean(elementType));
		}
	}

	/**
	 * 将JSONArray转换为集合
	 *
	 * @param json        JSONArray
	 * @param result      结果集合
	 * @param elementType 元素类型
	 */
	private void fill(final JSONArray json, final Collection<?> result, final Type elementType) {
		for (final JSON element : json) {
			result.add(element.toBean(elementType));
		}
	}
}
