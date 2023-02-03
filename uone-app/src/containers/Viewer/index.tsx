import { Auth } from "aws-amplify";
import { Loading } from "components";
import { FC, useEffect, useState } from "react";
import { useRecoilState, useRecoilValue } from "recoil";
import { appAtom, userAtom } from "state";
import useSWR from "swr";

import { AbilityContext } from "context/Ability/Can";
import { createAbilityFor } from "context/Ability/ability";
import { API } from "aws-amplify";

import config from "../../config";

const Viewer: FC = ({ children }) => {
  /**
   * Because Recoil doesn't see the atoms if it is not in App tree
   * and we are using lazy loading for any route component using atoms.
   *
   * Please put ALL your `statePersist` atoms here!!!
   */
  const [userAtomState, setUserAtomState] = useRecoilState(userAtom);
  const [isLoading, setIsLoading] = useState(true);

  const loadUserProfile = async () => {
    try {
      let path = `/entity/profileFindOne`;
      const data = await API.get(config.apiGateway.NAME, path, {});
      const {
        username,
        userId,
        roleId,
        roleName,
        clientId,
        //clientName,
        departments,
        profileImg,
        fullName,
        firstName,
        lastName,
        pointsBalance,
        pointsCumulative,
        kpis,
        modules,
        avatarImages,
        bannerImages,
        uone_data,
      } = data;

      const [firstDepartment] = departments || [];

      const entityData = await API.get(
        config.apiGateway.NAME,
        "/entity/list-entities-by-type/client",
        {}
      );

      const clientName = entityData?.attributes?.code;

      (window as any).Appcues.identify(userId, {
        roleName,
        clientName,
      });

      const currentSession = await Auth.currentSession();
      const idToken = currentSession.getIdToken();
      const jwtToken = idToken.getJwtToken();

      setUserAtomState({
        ...userAtomState,
        // Profile
        userId,
        roleId,
        roleName,
        clientId,
        clientName,
        departmentId: firstDepartment?.departmentId,
        departmentName: firstDepartment?.departmentName,
        username,
        profileImg,
        fullName,
        firstName,
        lastName,
        pointsBalance,
        pointsCumulative,
        kpis,
        modules,
        avatarImages,
        bannerImages,
        uoneData: uone_data,
        jwtToken,
      });

      setIsLoading(false);
    } catch (error) {
      console.error("Failed to fetch profile", error);
    }
  };
  const { userId, imageUploaded } = useRecoilValue(userAtom);
  const sendWizardCompleteMessage = async () => {
    try {
      let path = "/entity/wizard-completed/" + userId;
      await API.get(config.apiGateway.NAME, path, {});
    } catch (error) {
      console.error("Failed to fetch profile", error);
    }
  };

  useEffect(() => {
    (window as any).Appcues.on("flow_completed", function (event: any) {
      sendWizardCompleteMessage();
    });
  }, []);

  useEffect(() => {
    loadUserProfile();
  }, []);

  useEffect(() => {
    loadUserProfile();
  }, [imageUploaded]);

  if (isLoading) return <Loading isInProgress={true} />;

  const ability = createAbilityFor(userAtomState);
  return (
    <AbilityContext.Provider value={ability}>
      {children}
    </AbilityContext.Provider>
  );
};

export default Viewer;
