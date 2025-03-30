package cit.edu.wildcanteen

object FoodRepository {
    fun getPopularFoodList(): List<FoodItem> {
        return listOf(
            FoodItem("Main Meal","Inasal Chicken", 99.00, 4.8, "Mang Inasal Clone medyo masarap sa una worth it naman kasi unli rice!", "https://ik.imagekit.io/kylezzz/drawable/chicken.png"),
            FoodItem("Main Meal","Palabok", 80.00, 4.9, "Palabok na masarap din, lasang palabok sa jollibee na may pagka mang inasal!", "https://ik.imagekit.io/kylezzz/drawable/palabok.png?updatedAt=1743247449304"),
            FoodItem("Main Meal","Lumpia", 50.00, 4.6, "Lumpia na galing pa sa birthday ng kapit bahay mo haha!", "https://ik.imagekit.io/kylezzz/drawable/lumpia.png?updatedAt=1743247448996")
        )
    }

    fun getAllFoodList(): List<FoodItem> {
        return listOf(
            // Main Meals
            FoodItem("Main Meal", "Sinigang na Baboy", 120.00, 4.9, "Sour pork soup with vegetables, perfect for rainy days!", "https://ik.imagekit.io/kylezzz/drawable/sinigang.png?updatedAt=1743247451949"),
            FoodItem("Main Meal", "Adobo (Chicken/Pork)", 110.00, 4.8, "Classic soy sauce and vinegar stew with tender meat.", "https://ik.imagekit.io/kylezzz/drawable/adobo.jpg?updatedAt=1743247443868"),
            FoodItem("Main Meal", "Bicol Express", 115.00, 4.7, "Spicy coconut-based pork dish with chili peppers.","https://ik.imagekit.io/kylezzz/drawable/bicol_express.jpg?updatedAt=1743247444186"),
            FoodItem("Main Meal", "Pancit Canton", 20.00, 4.6, "Stir-fried noodles with vegetables and meat.", "https://ik.imagekit.io/kylezzz/drawable/pancit_canton.png?updatedAt=1743247449538"),
            FoodItem("Main Meal", "Lechon Kawali", 130.00, 4.9, "Crispy deep-fried pork belly served with liver sauce.", "https://ik.imagekit.io/kylezzz/drawable/lechon_kawali.jpg?updatedAt=1743247448757"),

            // Snacks & Sides
            FoodItem("Snack & Side", "Empanada", 45.00, 4.6, "Crispy pastry filled with meat, egg, and vegetables.","https://ik.imagekit.io/kylezzz/drawable/chicharon_bulaklak.jpg?updatedAt=1743247443961"),
            FoodItem("Snack & Side", "Turon", 35.00, 4.8, "Caramelized banana wrapped in a crunchy lumpia wrapper.", "https://ik.imagekit.io/kylezzz/drawable/turon.png?updatedAt=1743247453191"),
            FoodItem("Snack & Side", "Kwek-Kwek", 30.00, 4.7, "Quail eggs coated in orange batter and deep-fried.","https://ik.imagekit.io/kylezzz/drawable/kwek_kwek.png?updatedAt=1743247448617"),
            FoodItem("Snack & Side", "Chicharon Bulaklak", 60.00, 4.5, "Crispy deep-fried pork intestines, perfect with vinegar dip.", "https://ik.imagekit.io/kylezzz/drawable/chicharon_bulaklak.jpg?updatedAt=1743247443961"),
            FoodItem("Snack & Side", "Tokwa’t Baboy", 70.00, 4.6, "Fried tofu and pork with a tangy soy-vinegar sauce.", "https://ik.imagekit.io/kylezzz/drawable/tokwa_baboy.jpg?updatedAt=1743247453169"),

            // Beverages
            FoodItem("Beverage", "Sago’t Gulaman", 30.00, 4.8, "Sweet iced drink with tapioca pearls and jelly.","https://ik.imagekit.io/kylezzz/drawable/sagot_gulaman.jpg?updatedAt=1743247448830"),
            FoodItem("Beverage", "Buko Juice", 35.00, 4.9, "Fresh coconut juice, naturally refreshing!", "https://ik.imagekit.io/kylezzz/drawable/buko_juice.jpg?updatedAt=1743247443957"),
            FoodItem("Beverage", "Calamansi Juice", 30.00, 4.6, "Filipino lemon juice, perfect for boosting immunity.","https://ik.imagekit.io/kylezzz/drawable/calamansi_juice.jpg?updatedAt=1743247443990"),
            FoodItem("Beverage", "Gulaman at Sago Shake", 50.00, 4.8, "Creamy, blended version of the classic street drink.", "https://ik.imagekit.io/kylezzz/drawable/gulaman_shake.jpg?updatedAt=1743247448555"),

            // Desserts
            FoodItem("Dessert", "Leche Flan", 50.00, 4.9, "Creamy caramel custard, melt-in-your-mouth goodness!","https://ik.imagekit.io/kylezzz/drawable/leche_flan.jpg?updatedAt=1743247448836"),
            FoodItem("Dessert", "Halo-Halo", 90.00, 4.8, "Shaved ice dessert with mixed sweet ingredients and leche flan.", "https://ik.imagekit.io/kylezzz/drawable/halo_halo.jpg?updatedAt=1743247448727"),
            FoodItem("Dessert", "Ube Halaya", 60.00, 4.7, "Purple yam jam, rich and sweet!", "https://ik.imagekit.io/kylezzz/drawable/ube_halaya.png?updatedAt=1743247453260"),
            FoodItem("Dessert", "Cassava Cake", 45.00, 4.7, "Chewy, coconut-flavored cassava dessert.", "https://ik.imagekit.io/kylezzz/drawable/cassava_cake.jpeg?updatedAt=1743247444120")
        )
    }
}
