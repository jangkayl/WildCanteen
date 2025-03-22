package cit.edu.wildcanteen

import cit.edu.wildcanteen.data_class.FoodItem

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
}
