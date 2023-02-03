import {
  createContext,
  Dispatch,
  FC,
  SetStateAction,
  useContext,
  useState,
} from "react";

const LayoutContext =
  createContext<[string, Dispatch<SetStateAction<string>>] | undefined>(
    undefined
  );

const LayoutProvider: FC = ({ children }) => {
  const appState = useState("Dashboard");

  return (
    <LayoutContext.Provider value={appState}>{children}</LayoutContext.Provider>
  );
};

export const useLayoutContext = () => {
  const context = useContext(LayoutContext);
  if (context === undefined)
    throw new Error("useLayoutContext must be used within a LayoutProvider");
  const [title, setLayoutTitle] = context;
  return { title, setLayoutTitle };
};

export default LayoutProvider;
