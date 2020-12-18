package icecream.service.resource.model;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class PetData {
    private static List<String> breeds = new ArrayList<>();
    static {
        breeds.add("Afghan Hound");
        breeds.add("Beagle");
        breeds.add("Bernese Mountain Dog");
        breeds.add("Bloodhound");
        breeds.add("Dalmatian");
        breeds.add("Jack Russell Terrier");
        breeds.add("Norwegian Elkhound");
    }

    private static List<String> names = new ArrayList<>();
    static {
        names.add("Bailey");
        names.add("Bella");
        names.add("Max");
        names.add("Lucy");
        names.add("Winston");
        names.add("Rosie");
    }

    public static List<String> getBreeds() {
        return breeds;
    }

    public static List<String> getNames() {
        return names;
    }

    public static String getRandomBreed() {
        return breeds.get(ThreadLocalRandom.current().nextInt(0, breeds.size() - 1));
    }

    public static String getRandomName() {
        return names.get(ThreadLocalRandom.current().nextInt(0, names.size() - 1));
    }

    public static Date getRandomDoB() {
        GregorianCalendar gc = new GregorianCalendar();

        int year = ThreadLocalRandom.current().nextInt(
                Calendar.getInstance().get(Calendar.YEAR) - 15,
                Calendar.getInstance().get(Calendar.YEAR)
        );

        gc.set(Calendar.YEAR, year);

        int dayOfYear = ThreadLocalRandom.current().nextInt(1, gc.getActualMaximum(Calendar.DAY_OF_YEAR));

        gc.set(Calendar.DAY_OF_YEAR, dayOfYear);
        return gc.getTime();
    }
}
