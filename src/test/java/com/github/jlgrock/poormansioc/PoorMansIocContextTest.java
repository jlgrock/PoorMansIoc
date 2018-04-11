package com.github.jlgrock.poormansioc;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class PoorMansIocContextTest {

    public interface Animal {

    }

    public interface Feline extends Animal {

    }

    public interface Canine extends Animal {

    }

    public interface Pet {

    }

    public static class Cat implements Feline, Pet {

    }

    public static class Dog implements Canine, Pet {

    }

    public static class CatHouse {

        public Pet myPet() {
            return new Cat();
        }
    }

    public static class DogHouse {
        public Dog myBestFriend() {
            return new Dog();
        }
    }

    private PoorMansIocContext poorMansIocContext;

    @Before
    public void setup() {
        poorMansIocContext = PoorMansIoc.getContext();
        poorMansIocContext.clear();
    }
    @Test
    public void testAddConfigClassConreteImplementation() {
        poorMansIocContext.addConfigurationClass(CatHouse.class);
        Pet petByName = poorMansIocContext.getBeanByName("myPet");
        assertTrue(petByName != null);
        assertTrue(petByName instanceof Cat);
        Pet petByType = poorMansIocContext.getBeanByType(Pet.class);
        assertTrue(petByType != null);
        assertTrue(petByType instanceof Cat);
    }

    @Test
    public void testAddConfigClassInterfaceMatch() {
        poorMansIocContext.addConfigurationClass(DogHouse.class);
        Dog petByName = poorMansIocContext.getBeanByName("myBestFriend");
        assertTrue(petByName != null);
        Dog petByType = poorMansIocContext.getBeanByType(Dog.class);
        assertTrue(petByType != null);
        Pet petByType2 = poorMansIocContext.getBeanByType(Canine.class);
        assertTrue(petByType2 != null);
        assertTrue(petByType2 instanceof Dog);
    }

    @Test
    public void testAddBeanConreteImplementation() {
        poorMansIocContext.addBean(new CatHouse().myPet(), "myPet");
        Pet petByName = poorMansIocContext.getBeanByName("myPet");
        assertTrue(petByName != null);
        assertTrue(petByName instanceof Cat);
        Pet petByType = poorMansIocContext.getBeanByType(Pet.class);
        assertTrue(petByType != null);
        assertTrue(petByType instanceof Cat);
    }

    @Test
    public void testAddBeanClassInterfaceMatch() {
        poorMansIocContext.addBean(new DogHouse().myBestFriend(), "myBestFriend");
        Dog petByName = poorMansIocContext.getBeanByName("myBestFriend");
        assertTrue(petByName != null);
        Dog petByType = poorMansIocContext.getBeanByType(Dog.class);
        assertTrue(petByType != null);
        Pet petByType2 = poorMansIocContext.getBeanByType(Canine.class);
        assertTrue(petByType2 != null);
        assertTrue(petByType2 instanceof Dog);
    }

    @Test
    public void testMultipleInterfaceMatch() {
        poorMansIocContext.addConfigurationClass(CatHouse.class);
        poorMansIocContext.addConfigurationClass(DogHouse.class);

        List<Pet> pets = poorMansIocContext.getAllBeansByType(Pet.class);
        assertThat(pets.size(), equalTo(2));

        Dog petByName = poorMansIocContext.getBeanByName("myBestFriend");
        assertTrue(petByName != null);
        Dog petByType = poorMansIocContext.getBeanByType(Dog.class);
        assertTrue(petByType != null);
        Pet petByType2 = poorMansIocContext.getBeanByType(Canine.class);
        assertTrue(petByType2 != null);
        assertTrue(petByType2 instanceof Dog);

        Pet petByName2 = poorMansIocContext.getBeanByName("myPet");
        assertTrue(petByName2 != null);
        assertTrue(petByName2 instanceof Cat);
        Pet petByType3 = poorMansIocContext.getBeanByType(Cat.class);
        assertTrue(petByType3 != null);
        assertTrue(petByType3 instanceof Cat);

    }

    @Test
    public void testMultipleInterfaceMatchWithQualifier() {
        poorMansIocContext.addBean(new DogHouse().myBestFriend(), "myBestFriend", "myQual");
        poorMansIocContext.addBean(new CatHouse().myPet(), "myPet");

        List<Pet> pets = poorMansIocContext.getAllBeansByType(Pet.class);
        assertThat(pets.size(), equalTo(2));

        Pet pet = poorMansIocContext.getBeanByType(Pet.class, "myQual");
        assertTrue(pet instanceof Dog);
    }

}