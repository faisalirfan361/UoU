import { APP_ATOM_KEY } from "./app";
import { USER_ATOM_KEY } from "./user";

export const heyDayNowPersistState = {
  key: "heyDayNowPersistState",
  atomsToPersist: [APP_ATOM_KEY, USER_ATOM_KEY],
};

if (process.env.NODE_ENV === "development") {
  console.log(
    "\n=== State - heyDayNowPersistState ===\n",
    heyDayNowPersistState
  );
}
