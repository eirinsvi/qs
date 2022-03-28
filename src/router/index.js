import { createRouter, createWebHistory } from "vue-router";
import LoginStudent from "../views/LoginStudent.vue";
import LoginAdministrator from "../views/LoginAdministrator.vue";

const routes = [
  {
    path: "/",
    name: "Log in as student",
    component: LoginStudent,
  },
  {
    path: "/administrator",
    name: "Log in as administrator",
    component: LoginAdministrator,
  },
];

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes,
});

export default router;
