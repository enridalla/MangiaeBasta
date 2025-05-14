package com.example.mangiaebasta.models

import kotlinx.coroutines.delay

/**
 * Modello dati e repository fittizio.
 */
data class MenuModel(
    val mid: Int,
    val name: String,
    val shortDescription: String,
    val longDescription: String,
    val price: Double,
    val deliveryTime: Int,
    val image: String? = null
) {
    companion object {
        /** Pixel rosso 16×16 PNG su una sola riga (niente spazi o \n). */
        private const val PLACEHOLDER_BASE64 =
            "iVBORw0KGgoAAAANSUhEUgAAAA4AAAAOCAIAAACQKrqGAAAACXBIWXMAAAsSAAALEgHS3X78AAAA" +
                    "B3RJTUUH6AYGCgUIos5jUwAAAB1pVFh0Q29tbWVudAAAAAAAQ3JlYXRlZCB3aXRoIEdJTVBXgQ4X" +
                    "AAABGklEQVQ4y7XSMUoDQRQG4Gf+TbxAQUHBzU0Gh1KQ8hUlC4uFH0EVBBwEVkUhkFFEEbdgYiaC" +
                    "CMqS8gaQm6Cn2MPdw88z75w77337jnjfj1iZqJumGco1hzVPcYzTKOorHhVVEVFKG2SmMYx1xQn+" +
                    "EQ3wnl1dScGx3sIwDxB0Mypxnl6/jHB5DT8IVVdF0H1fRuY7gdZ03TG93w9pE8lKgLxVi+4DJH5R" +
                    "KqXOUP8sw4Ck05wcN9GI8RBTi7yuD9KkSLQU6vq/JgXiu03Yht+oF67xK1pZM38B4wT9tgM5U4FC" +
                    "5gp9WAHA9sDm0bXX5u2nZ8DhUlpXnOBp5P+Sul2TaQytMJfV5u3fcAQtT/mo+KJ6R/nkAOzU32nA" +
                    "Cb4k+wFHg+hVdV0zbQAAAABJRU5ErkJggg=="

        /** Dati mock. */
        private val sampleMenus = listOf(
            MenuModel(
                mid = 1,
                name = "Pizza Margherita",
                shortDescription = "Pomodoro, mozzarella e basilico fresco",
                longDescription = "La regina delle pizze con pomodoro San Marzano, mozzarella fior di latte e basilico.",
                price = 8.50,
                deliveryTime = 30,
                image = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAIAAACQd1PeAAAADElEQVR4nGP4z8AAAAMBAQDJ/pLvAAAAAElFTkSuQmCC"
            ),
            MenuModel(
                mid = 2,
                name = "Hamburger Gourmet",
                shortDescription = "Manzo 200 g, cheddar, bacon e salsa BBQ",
                longDescription = "Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQHamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale. artigianale.Hamburger di manzo 100% italiano, cheddar, bacon croccante e salsa BBQ artigianale.",
                price = 11.00,
                deliveryTime = 25,
                image = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAIAAACQd1PeAAAADElEQVR4nGP4z8AAAAMBAQDJ/pLvAAAAAElFTkSuQmCC"
            ),
            MenuModel(
                mid = 3,
                name = "Insalata Greca",
                shortDescription = "Feta, olive kalamata, pomodorini e cetrioli",
                longDescription = "Feta DOP, olive kalamata, pomodorini, cetrioli e origano.",
                price = 7.00,
                deliveryTime = 15,
                image = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAIAAACQd1PeAAAADElEQVR4nGP4z8AAAAMBAQDJ/pLvAAAAAElFTkSuQmCC"
            )
        )

        /** Simula chiamate API. */
        suspend fun getAll(): List<MenuModel> { delay(400); return sampleMenus }
        suspend fun getById(id: Int): MenuModel? { delay(150); return sampleMenus.find { it.mid == id } }
    }
}
