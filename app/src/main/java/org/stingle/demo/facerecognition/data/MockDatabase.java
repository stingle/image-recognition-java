package org.stingle.demo.facerecognition.data;

import org.stingle.facerecoginition.Person;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MockDatabase {
	private static final MockDatabase INSTANCE = new MockDatabase();
	public static MockDatabase getInstance() {
		return INSTANCE;
	}

	private final List<Image> imageList;
	private final Map<UUID, String> personNameMap;

	private final Map<UUID, Person> personMap;

	private MockDatabase() {
		imageList = new ArrayList<>();
		personNameMap = new HashMap<>();
		personMap = new HashMap<>();
	}

	public Collection<Person> getPersonCollection() {
		return personMap.values();
	}

	public void addPersonList(List<Person> personList) {
		for (Person person : personList) {
			personMap.put(person.id, person);
		}
	}

	public void addImage(Image image) {
		imageList.add(image);
	}

	public List<Image> getImageList() {
		return imageList;
	}

	public List<Image> getImageListWithPerson(UUID personId) {
		List<Image> imageListWithPerson = new ArrayList<>();

		for (Image image : imageList) {
			if (image.personRectMap.containsKey(personId)) {
				imageListWithPerson.add(image);
			}
		}

		return imageListWithPerson;
	}

	public String getPersonName(UUID id) {
		if (personNameMap.containsKey(id)) {
			return personNameMap.get(id);
		} else {
			return "Unknown";
		}
	}

	public void setPersonName(UUID id, String name) {
		personNameMap.put(id, name);
	}

	public List<PersonAndImages> getPersonAndImagesList() {
		List<PersonAndImages> personAndImagesList = new ArrayList<>();

		for (Person person : personMap.values()) {
			List<Image> imageWithPersonList = getImageListWithPerson(person.id);

			personAndImagesList.add(new PersonAndImages(person, imageWithPersonList));
		}

		return personAndImagesList;
	}
}
