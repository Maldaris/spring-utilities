package edu.stetson.managed.impl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.springframework.ui.Model;

import edu.stetson.managed.ModelMapper;
import edu.stetson.util.Util;

public class ManagedModelMapper implements ModelMapper<ManagedModel> {

	@Override
	public Model mapObjectToModel(Model model, String name, ManagedModel res) {

		model.addAttribute(name, res.getUnderlyingObject());

		return model;
	}

	@Override
	public Model unwrapObjectToModel(Model model, ManagedModel res) {
		Field[] fields = res.getUnderlyingClass().getDeclaredFields();

		for (Field field : fields) {
			Method m;
			Object attr;
			try {
				m = res.getUnderlyingClass().getDeclaredMethod(
						Util.prependPrefix("get", field.getName()),
						new Class<?>[] {});
				attr = m.invoke(res.getUnderlyingObject(), new Object[] {});
			} catch (SecurityException
					| IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
				return model;
			} catch (NoSuchMethodException e){
				e.printStackTrace();
				continue;
			}
			model.addAttribute(field.getName(), attr);
		}

		return model;
	}
}
