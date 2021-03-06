package com.windforce.common.resource.excel;

import com.windforce.common.resource.Storage;
import com.windforce.common.resource.StorageManager;
import com.windforce.common.resource.anno.Static;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@Component
public class StaticTestTarget implements InitializingBean {

	@Autowired
	private StorageManager resourceManager;
	@Static
	private Storage<Integer, Human> storage;
	@Static(value = "1")
	private Human human;

	@PostConstruct
	protected void initilize() {
		assertThat(resourceManager, notNullValue());
		assertThat(storage, notNullValue());
		assertThat(human, notNullValue());
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		assertThat(resourceManager, notNullValue());
		assertThat(storage, notNullValue());
		assertThat(human, notNullValue());
	}

}
