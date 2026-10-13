import { useContext } from "react";
import { AuthContext } from "@/AuthContext";

//     };
//   };
// }

export default function useUser() {
  const context = useContext(AuthContext);

  return { ...context.store };
}
