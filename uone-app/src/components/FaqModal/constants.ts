export const faqs = [
  {
    question: "What do I need to level up?",
    answer:
      "Table",
  },
  {
    question: "How can I set up my password?",
    answer:
      "After your profile is set up you will receive an email with a temporary password and steps to set up your own password.",
  },
  {
    question: "How do I change my password?",
    answer:
      "On the sign in page under password you will see a 'reset password' icon, click and follow the the steps.",
  },
  {
    question: "How can I customize my banner?",
    answer:
      "You can customize your banner by clicking on the pencil icon located on the top right corner of the banner.",
  },
  {
    question: "How can I change my avatar/profile pic?",
    answer:
      "To upload a new or change your avatar picture click on the pencil icon below the avatar circle .",
  },
  {
    question: "Can I send a message to another team member or admin?",
    answer:
      "No, as of right now there is no messaging system in place. COMING SOON!",
  },
  {
    question: "How do I check my challenges/challenge results.",
    answer:
      "Click on 'Game Tab' there you will see all past challenges and current challenges- Bell notification will inform you of your results also.",
  },
  {
    question: "How do I check who the top 3 on my team are?",
    answer:
      "To check your team's top 3 point leaders click on 'Leader Board' on your navigation bar.",
  },
  {
    question: "How do I check who is on my team?",
    answer:
      "To check who is on your team, click on 'My Team' and scroll down to 'My Team Members', there it will show all the agents that are part of your team.",
  },
  {
    question: "How do I know what my coin balance is?",
    answer:
      "Your coin balance will appear on the top right corner of your main profile page.",
  },
  {
    question: "How can I redeem my coins?",
    answer:
      "To redeem your coins, click on 'coin Store' on your navigation bar, select your item by clicking 'get item' and click redeem.",
  },
  {
    question: "How do I see my teams KPI?",
    answer:
      "Click on 'My Team' tab and underneath your HUD will display you teams KPI cards.",
  },
  {
    question: "What does my level mean?",
    answer:
      "Levels represent your experience in the 'Game' section. The more points you accrue the higher your experience and the higher your level will be.",
  },
  {
    question: "How do I add new items for the coin store redemption?",
    answer:
      "To add new items to the store click on 'Coin Store' on your navigation bar, select 'add new item', add title, upload an image of the item, set value, and enter description then click 'add'.",
  },
  {
    question: "How do I set the coin budget?",
    answer:
      "To add new items to the store click on 'Coin Store' on your navigation bar, select 'add new item', add title, upload an image of the item, set value, and enter description then click 'add'.",
  },
  {
    question: "How do I set a KPI goal?",
    answer:
      "To set a KPI goal, click on 'Metric/KPI' on your Dashboard. There it will show you each metric, you can edit these by clicking on the 'pencil' icon located on the top right corner of each card.",
  },
  {
    question: "How do I create a challenge?",
    answer: `<ol>
        <li>click the "create' tab</li>
        <li>click the "Metric/kPI" field, select KPI from the list</li>
        <li>Type the name of the challenge in the title field</li>
        <li>Type the description or goal in the "Description" field</li>
        <li>Enter the coins you wish to award the winner in the "Winner Coins" field.</li>
        <li>Use the date picker to select the duration of the challenge</li>
        <li>Click on the "Add Participants" field to add as many people as you would like to the challenge.</li>
      </ol>`,
  },
  {
    question: "How I add/change permissions and roles for each user?",
    answer:
      "To change each users role or permission click on 'Settings' and Under Roles & Permissions tab click on the role you would like to change/update by clicking each value 'On' or 'Off'. These changes will automatically update permission across your company.",
  },
] as const;

import roundGrey from "../../assets/img/levelInfoTable/round-grey.png";
import dArrowGrey from "../../assets/img/levelInfoTable/double-arrows-grey.png";
import dArrowCyan from "../../assets/img/levelInfoTable/double-arow-cyan.png";
import bike from "../../assets/img/levelInfoTable/bike.png";
import hand from "../../assets/img/levelInfoTable/hand.png";
import arrowUp from "../../assets/img/levelInfoTable/arrow-up.png";
import easterEgg from "../../assets/img/levelInfoTable/easter-egg.png";
import butterfly from "../../assets/img/levelInfoTable/butterfly.png";
import crest from "../../assets/img/levelInfoTable/Crest.png";

export const levelTableInfoConst = [
  {
    behaviorReq: "No requirement",
    ptsReq: 0,
    badgeEarned: {
      image: roundGrey,
      txt: 'None'
    },
    ptsEarned: {
      image: dArrowGrey,
      txt: '0'
    },
    customUnlocked: {
      image: '',
      txt: ''
    }
  },
  {
    behaviorReq: "User participe in the Set up Wizard",
    ptsReq: 0,
    badgeEarned: {
      image: '',
      txt: 'Wizard badge'
    },
    ptsEarned: {
      image: arrowUp,
      txt: '100'
    },
    customUnlocked: {
      image: bike,
      txt: 'Bike track HUD'
    }
  },
  {
    behaviorReq: "User uploads an image",
    ptsReq: 100,
    badgeEarned: {
      image: hand,
      txt: 'High 5 badge'
    },
    ptsEarned: {
      image: arrowUp,
      txt: '100'
    },
    customUnlocked: {
      image: '',
      txt: ''
    }
  },
  {
    behaviorReq: "User gives 10 likes in the Feed",
    ptsReq: 250,
    badgeEarned: {
      image: butterfly,
      txt: 'Social Butterfly'
    },
    ptsEarned: {
      image: dArrowCyan,
      txt: '0'
    },
    customUnlocked: {
      image: '',
      txt: 'Theme 1'
    }
  },
  {
    behaviorReq: "User participates in 5 Challenges",
    ptsReq: 420,
    badgeEarned: {
      image: crest,
      txt: 'Chief Challenger'
    },
    ptsEarned: {
      image: arrowUp,
      txt: '50'
    },
    customUnlocked: {
      image: easterEgg,
      txt: 'Easter egg'
    }
  }
] as const;