package cit.edu.wildcanteen

object FoodRepository {
    fun getPopularFoodList(): List<FoodItem> {
        return listOf(
            FoodItem("Inasal Chicken", 99.00, 4.8, "Mang Inasal Clone medyo masarap sa una worth it naman kasi unli rice!", R.drawable.chicken),
            FoodItem("Palabok", 80.00, 4.9, "Palabok na masarap din, lasang palabok sa jollibee na may pagka mang inasal!", R.drawable.palabok),
            FoodItem("Lumpia", 50.00, 4.6, "Lumpia na galing pa sa birthday ng kapit bahay mo haha!", R.drawable.lumpia)
        )
    }

    fun getAllFoodList(): List<FoodItem> {
        return listOf(
            FoodItem("Inasal Chicken", 99.00, 4.8, "Mang Inasal Clone medyo masarap sa una worth it naman kasi unli rice!", R.drawable.chicken),
            FoodItem("Palabok", 80.00, 4.9, "Palabok na masarap din, lasang palabok sa jollibee na may pagka mang inasal!", R.drawable.palabok),
            FoodItem("Lumpia", 50.00, 4.6, "Lumpia na galing pa sa birthday ng kapit bahay mo haha!", R.drawable.lumpia),
            FoodItem("Inasal Chicken", 99.00, 4.8, "Mang Inasal Clone medyo masarap sa una worth it naman kasi unli rice!", R.drawable.chicken),
            FoodItem("Palabok", 80.00, 4.9, "Palabok na masarap din, lasang palabok sa jollibee na may pagka mang inasal!", R.drawable.palabok),
            FoodItem("Lumpia", 50.00, 4.6, "Lumpia na galing pa sa birthday ng kapit bahay mo haha!", R.drawable.lumpia),
            FoodItem("Inasal Chicken", 99.00, 4.8, "Mang Inasal Clone medyo masarap sa una worth it naman kasi unli rice!", R.drawable.chicken),
            FoodItem("Palabok", 80.00, 4.9, "Palabok na masarap din, lasang palabok sa jollibee na may pagka mang inasal!", R.drawable.palabok),
            FoodItem("Lumpia", 50.00, 4.6, "Lumpia na galing pa sa birthday ng kapit bahay mo haha!", R.drawable.lumpia),
            FoodItem("Inasal Chicken", 99.00, 4.8, "Mang Inasal Clone medyo masarap sa una worth it naman kasi unli rice!", R.drawable.chicken),
            FoodItem("Palabok", 80.00, 4.9, "Palabok na masarap din, lasang palabok sa jollibee na may pagka mang inasal!", R.drawable.palabok),
            FoodItem("Lumpia", 50.00, 4.6, "Lumpia na galing pa sa birthday ng kapit bahay mo haha!", R.drawable.lumpia)
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
