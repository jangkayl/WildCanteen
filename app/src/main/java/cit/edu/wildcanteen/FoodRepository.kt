package cit.edu.wildcanteen

object FoodRepository {
    fun getPopularFoodList(): List<FoodItem> {
        return listOf(
            FoodItem("Main Meal","Inasal Chicken", 99.00, 4.8, "Mang Inasal Clone medyo masarap sa una worth it naman kasi unli rice!", R.drawable.chicken),
            FoodItem("Main Meal","Palabok", 80.00, 4.9, "Palabok na masarap din, lasang palabok sa jollibee na may pagka mang inasal!", R.drawable.palabok),
            FoodItem("Main Meal","Lumpia", 50.00, 4.6, "Lumpia na galing pa sa birthday ng kapit bahay mo haha!", R.drawable.lumpia)
        )
    }

    fun getAllFoodList(): List<FoodItem> {
        return listOf(
            // Main Meals
            FoodItem("Main Meal", "Sinigang na Baboy", 120.00, 4.9, "Sour pork soup with vegetables, perfect for rainy days!", R.drawable.sinigang),
            FoodItem("Main Meal", "Adobo (Chicken/Pork)", 110.00, 4.8, "Classic soy sauce and vinegar stew with tender meat.", R.drawable.adobo),
            FoodItem("Main Meal", "Bicol Express", 115.00, 4.7, "Spicy coconut-based pork dish with chili peppers.", R.drawable.bicol_express),
            FoodItem("Main Meal", "Pancit Canton", 20.00, 4.6, "Stir-fried noodles with vegetables and meat.", R.drawable.pancit_canton),
            FoodItem("Main Meal", "Lechon Kawali", 130.00, 4.9, "Crispy deep-fried pork belly served with liver sauce.", R.drawable.lechon_kawali),

            // Snacks & Sides
            FoodItem("Snack & Side", "Empanada", 45.00, 4.6, "Crispy pastry filled with meat, egg, and vegetables.", R.drawable.empanada),
            FoodItem("Snack & Side", "Turon", 35.00, 4.8, "Caramelized banana wrapped in a crunchy lumpia wrapper.", R.drawable.turon),
            FoodItem("Snack & Side", "Kwek-Kwek", 30.00, 4.7, "Quail eggs coated in orange batter and deep-fried.", R.drawable.kwek_kwek),
            FoodItem("Snack & Side", "Chicharon Bulaklak", 60.00, 4.5, "Crispy deep-fried pork intestines, perfect with vinegar dip.", R.drawable.chicharon_bulaklak),
            FoodItem("Snack & Side", "Tokwa’t Baboy", 70.00, 4.6, "Fried tofu and pork with a tangy soy-vinegar sauce.", R.drawable.tokwa_baboy),

            // Beverages
            FoodItem("Beverage", "Sago’t Gulaman", 30.00, 4.8, "Sweet iced drink with tapioca pearls and jelly.", R.drawable.sagot_gulaman),
            FoodItem("Beverage", "Buko Juice", 35.00, 4.9, "Fresh coconut juice, naturally refreshing!", R.drawable.buko_juice),
            FoodItem("Beverage", "Calamansi Juice", 30.00, 4.6, "Filipino lemon juice, perfect for boosting immunity.", R.drawable.calamansi_juice),
            FoodItem("Beverage", "Gulaman at Sago Shake", 50.00, 4.8, "Creamy, blended version of the classic street drink.", R.drawable.gulaman_shake),

            // Desserts
            FoodItem("Dessert", "Leche Flan", 50.00, 4.9, "Creamy caramel custard, melt-in-your-mouth goodness!", R.drawable.leche_flan),
            FoodItem("Dessert", "Halo-Halo", 90.00, 4.8, "Shaved ice dessert with mixed sweet ingredients and leche flan.", R.drawable.halo_halo),
            FoodItem("Dessert", "Ube Halaya", 60.00, 4.7, "Purple yam jam, rich and sweet!", R.drawable.ube_halaya),
            FoodItem("Dessert", "Cassava Cake", 45.00, 4.7, "Chewy, coconut-flavored cassava dessert.", R.drawable.cassava_cake)
        )
    }

    fun getOrderLists(): List<Order> {
        val foodItems = getAllFoodList()

        return listOf(
            Order("ORD-001", foodItems[0], 1, totalAmount = foodItems[0].price, timestamp = System.currentTimeMillis()),
            Order("ORD-002", foodItems[1], 1, totalAmount = foodItems[1].price, timestamp = System.currentTimeMillis()),
            Order("ORD-003", foodItems[2], 1, totalAmount = foodItems[2].price, timestamp = System.currentTimeMillis())
        )
    }

}
