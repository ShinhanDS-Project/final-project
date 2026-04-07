// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import "@openzeppelin/contracts/token/ERC20/ERC20.sol";
import "@openzeppelin/contracts/access/Ownable.sol";

contract CharityDonationToken is ERC20, Ownable {
    address public hotWallet;
    address public coldWallet;

    uint256 public constant DENOMINATOR = 10000;

    event TokenAllocated(
        address indexed userWallet,
        uint256 indexed donationId,
        uint256 amount
    );

    event DonationSent(
        address indexed donorWallet,
        address indexed campaignWallet,
        uint256 indexed campaignId,
        uint256 donationId,
        uint256 amount
    );

    event CampaignSettled(
        uint256 indexed campaignId,
        address indexed campaignWallet,
        address indexed charityWallet,
        address beneficiaryWallet,
        uint256 totalAmount,
        uint256 feeBps,
        uint256 charityAmount,
        uint256 beneficiaryAmount,
        uint256 settlementId
    );

    event RedemptionReturned(
        address indexed requesterWallet,
        address indexed hotWallet,
        uint256 indexed redemptionId,
        uint256 amount
    );

    event HotWalletChanged(
        address indexed oldHotWallet,
        address indexed newHotWallet
    );

    event ColdWalletChanged(
        address indexed oldColdWallet,
        address indexed newColdWallet
    );

    event HotWalletRefilled(
        address indexed coldWallet,
        address indexed hotWallet,
        uint256 amount
    );

    constructor(
        string memory name_,
        string memory symbol_,
        uint256 initialSupply_,
        address hotWallet_,
        address coldWallet_
    ) ERC20(name_, symbol_) Ownable(msg.sender) {
        require(hotWallet_ != address(0), "Invalid hot wallet");
        require(coldWallet_ != address(0), "Invalid cold wallet");

        hotWallet = hotWallet_;
        coldWallet = coldWallet_;

        _mint(coldWallet_, initialSupply_);
    }

    function setHotWallet(address newHotWallet) external onlyOwner {
        require(newHotWallet != address(0), "Invalid hot wallet");

        address oldHotWallet = hotWallet;
        hotWallet = newHotWallet;

        emit HotWalletChanged(oldHotWallet, newHotWallet);
    }

    function setColdWallet(address newColdWallet) external onlyOwner {
        require(newColdWallet != address(0), "Invalid cold wallet");

        address oldColdWallet = coldWallet;
        coldWallet = newColdWallet;

        emit ColdWalletChanged(oldColdWallet, newColdWallet);
    }

    function refillHotWallet(uint256 amount) external onlyOwner {
        require(amount > 0, "Amount must be greater than 0");
        require(
            balanceOf(coldWallet) >= amount,
            "Insufficient cold wallet balance"
        );

        _transfer(coldWallet, hotWallet, amount);

        emit HotWalletRefilled(coldWallet, hotWallet, amount);
    }

    function allocateToUser(
        address userWallet,
        uint256 amount,
        uint256 donationId
    ) external onlyOwner {
        require(userWallet != address(0), "Invalid user wallet");
        require(amount > 0, "Amount must be greater than 0");
        require(
            balanceOf(hotWallet) >= amount,
            "Insufficient hot wallet balance"
        );

        _transfer(hotWallet, userWallet, amount);

        emit TokenAllocated(userWallet, donationId, amount);
    }

    function donateToCampaign(
        address campaignWallet,
        uint256 amount,
        uint256 campaignId,
        uint256 donationId
    ) external {
        require(campaignWallet != address(0), "Invalid campaign wallet");
        require(amount > 0, "Amount must be greater than 0");
        require(
            balanceOf(msg.sender) >= amount,
            "Insufficient user balance"
        );

        _transfer(msg.sender, campaignWallet, amount);

        emit DonationSent(
            msg.sender,
            campaignWallet,
            campaignId,
            donationId,
            amount
        );
    }

    function settleCampaign(
        address charityWallet,
        address beneficiaryWallet,
        uint256 totalAmount,
        uint256 feeBps,
        uint256 campaignId,
        uint256 settlementId
    ) external {
        require(charityWallet != address(0), "Invalid charity wallet");
        require(beneficiaryWallet != address(0), "Invalid beneficiary wallet");
        require(totalAmount > 0, "Amount must be greater than 0");
        require(feeBps <= DENOMINATOR, "Invalid feeBps");
        require(
            balanceOf(msg.sender) >= totalAmount,
            "Insufficient campaign balance"
        );

        uint256 charityAmount = (totalAmount * feeBps) / DENOMINATOR;
        uint256 beneficiaryAmount = totalAmount - charityAmount;

        _transfer(msg.sender, charityWallet, charityAmount);
        _transfer(msg.sender, beneficiaryWallet, beneficiaryAmount);

        emit CampaignSettled(
            campaignId,
            msg.sender,
            charityWallet,
            beneficiaryWallet,
            totalAmount,
            feeBps,
            charityAmount,
            beneficiaryAmount,
            settlementId
        );
    }

    function returnForRedemption(
        uint256 amount,
        uint256 redemptionId
    ) external {
        require(amount > 0, "Amount must be greater than 0");
        require(
            balanceOf(msg.sender) >= amount,
            "Insufficient requester balance"
        );

        _transfer(msg.sender, hotWallet, amount);

        emit RedemptionReturned(
            msg.sender,
            hotWallet,
            redemptionId,
            amount
        );
    }

    function getWalletBalance(address wallet) external view returns (uint256) {
        require(wallet != address(0), "Invalid wallet");
        return balanceOf(wallet);
    }

    function getHotWalletBalance() external view returns (uint256) {
        return balanceOf(hotWallet);
    }

    function getColdWalletBalance() external view returns (uint256) {
        return balanceOf(coldWallet);
    }
}
